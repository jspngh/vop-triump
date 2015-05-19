package be.ugent.vop.ui.venue;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.melnykov.fab.FloatingActionButton;

import java.util.List;

import be.ugent.vop.R;
import be.ugent.vop.backend.loaders.AsyncResult;
import be.ugent.vop.backend.loaders.CheckInLoader;
import be.ugent.vop.backend.loaders.RankingLoader;
import be.ugent.vop.backend.myApi.model.RankingBean;
import be.ugent.vop.utils.PrefUtils;

public class VenueRankingFragment extends Fragment implements VenueActivity.VenueActivityCallback {

    private String TAG = "VenueRankingF";

    private FloatingActionButton checkinButton;
    private List<RankingBean> ranking;
    private RecyclerView rankingView;
    private TextView noCheckinYet;
    private TextView noRankingForFilter;
    private Context context;
    private Spinner groupTypeSpinner;
    private Spinner groupSizeSpinner;
    private View mSpinners;
    protected RecyclerView.LayoutManager mLayoutManager;
    protected RankingAdapter mAdapter;

    private Palette mPalette;
    private boolean mCanCheckIn = false;

    private String currentGroupType = "All";
    private String currentGroupSize = "All";

    private int customMin = 1;
    private int customMax = Integer.MAX_VALUE;
    private boolean checkinPending = false;

    private int groupTypePos;
    private int groupSize;

    protected SwipeRefreshLayout mSwipeRefreshLayout;

    private String fsVenueId;

    private VenueActivity mActivity;

    @Override
    public void setColorPalette(Palette p) {
        if(p != null){
            mPalette = p;

            mAdapter.setBackgroundColor(p.getVibrantSwatch().getRgb());
            mAdapter.setTextColor(p.getVibrantSwatch().getBodyTextColor());
        }
    }

    public void setCheckinAvailable(boolean canCheckIn){
        mCanCheckIn = canCheckIn;
        if(mCanCheckIn || PrefUtils.isGodeMode(getActivity()))
            checkinButton.setVisibility(View.VISIBLE);
        else
            checkinButton.setVisibility(View.GONE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_venue_ranking, container, false);

        context = getActivity();

        //    noRankingTextView = (TextView) rootView.findViewById(R.id.textViewNoRanking);
        rankingView = (RecyclerView) rootView.findViewById(R.id.ranking_list);
        checkinButton = (FloatingActionButton)rootView.findViewById(R.id.buttonCheckin);
        mSpinners = rootView.findViewById(R.id.spinners);

        noCheckinYet = (TextView) rootView.findViewById(R.id.no_checkin_yet);
        noRankingForFilter = (TextView) rootView.findViewById(R.id.no_ranking_for_filter);

        /**
         * setup recycler view
         */
        mLayoutManager = new LinearLayoutManager(getActivity());
        rankingView.setLayoutManager(mLayoutManager);
        rankingView.setItemAnimator(new DefaultItemAnimator());
        checkinButton.attachToRecyclerView(rankingView, null, new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                // TODO: this still needs some fixing
                if(mActivity != null)
                    mActivity.onScroll(dx, dy, mSpinners);
            }
        });

        checkinButton.setVisibility(View.GONE);

        int ht = 200;
        int wt = 200;

        float ht_px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, ht, getResources().getDisplayMetrics());
        float wt_px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, wt, getResources().getDisplayMetrics());

        mAdapter = new RankingAdapter(getActivity(), null, Color.BLACK, Color.WHITE, (int)wt_px, (int)ht_px, true);
        rankingView.setAdapter(mAdapter);


        /**
         *
         * Listview refresh
         */
        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.fragment_venue_swipe_refresh);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(!checkinPending)
                    getLoaderManager().restartLoader(1,null,mRankingLoaderListener);
            }
        });
        mSwipeRefreshLayout.setColorSchemeResources(R.color.theme_primary_dark);


        //checkinButton.attachToRecyclerView(rankingView);


        /**
         * Populate the spinners
         * groupType
         */
        groupTypeSpinner = (Spinner) rootView.findViewById(R.id.spinnerGroupType);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapterType = ArrayAdapter.createFromResource(context,
                R.array.groupType_spinner_options, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapterType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        groupTypeSpinner.setAdapter(adapterType);

        /**
         * groupSize
         */
        groupSizeSpinner = (Spinner) rootView.findViewById(R.id.spinnerGroupSize);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapterSize = ArrayAdapter.createFromResource(context,
                R.array.groupSize_spinner_options, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapterSize.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        groupSizeSpinner.setAdapter(adapterSize);



        if(getArguments().containsKey(VenueActivity.VENUE_ID))
            fsVenueId = getArguments().getString(VenueActivity.VENUE_ID);

        Log.d("VenueFragment", "venueId :" + fsVenueId);

        /**
         *
         * Initialize loaders
         */
        //loader for ranking (to backend)
        getLoaderManager().initLoader(0, null, mRankingLoaderListener);

        checkinButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Checkin button clicked");
                getLoaderManager().restartLoader(1, null, mCheckInLoaderListener);
            }
        });

        groupSizeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selected = (String) parentView.getSelectedItem();
                Log.d("VenueFragment", "selected group size: "+selected);
                if(parentView.getSelectedItemPosition() == 4){
                    currentGroupSize = selected;
                    SizeSelectorDialog dialog = new SizeSelectorDialog();
                    dialog.setListener(mSizeSelectorListener);
                    dialog.show(getFragmentManager(), "sizeselector");
                }else if(!currentGroupSize.equals(selected)) {
                    currentGroupSize = selected;
                    getLoaderManager().restartLoader(0, null, mRankingLoaderListener);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

        groupTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selected = (String) parentView.getSelectedItem();
                Log.d("VenueFragment", "selected group size: "+selected);
                if(!currentGroupType.equals(selected)){
                    currentGroupType = selected;
                    getLoaderManager().restartLoader(0,null, mRankingLoaderListener);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // your code here
            }

        });

        return rootView;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setCheckinAvailable(mActivity.canCheckIn());
        //rankingListView.setOnScrollListener(this);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if(activity instanceof VenueActivity)
            mActivity = (VenueActivity) activity;

    }

    /***********
     Loaders
     ***********/

    /**
     *
     * Loader 1: Ranking
     *
     */

    private LoaderManager.LoaderCallbacks<List<RankingBean>> mRankingLoaderListener
            = new LoaderManager.LoaderCallbacks<List<RankingBean>>() {

        @Override
        public void onLoadFinished(Loader<List<RankingBean>> loader, List<RankingBean> rankings) {
            Log.d("VenueFragment", "onLoadFinished rankingloader");
            ranking=rankings;
            if(rankings!=null){
                rankingView.setVisibility(View.VISIBLE);
                noRankingForFilter.setVisibility(View.GONE);
                noCheckinYet.setVisibility(View.GONE);

                for(RankingBean r:rankings){
                    Log.d("VenueFragment",r.getGroupBean().getName()+ " | "+r.getPoints());
                }

                mAdapter.setRankings(rankings);
                mAdapter.notifyDataSetChanged();
            }else{
                rankingView.setVisibility(View.INVISIBLE);
                if(groupSize == 0 && groupTypePos == 0){
                    noCheckinYet.setVisibility(View.VISIBLE);
                }else{
                    noRankingForFilter.setVisibility(View.VISIBLE);
                }
                //   noRankingTextView.setText(R.string.no_ranking);
            }

            mSwipeRefreshLayout.setRefreshing(false);
        }

        @Override
        public Loader<List<RankingBean>> onCreateLoader(int id, Bundle args) {
            Log.d("venueFragment", "onCreateLoader");
            groupTypePos = groupTypeSpinner.getSelectedItemPosition();
            String groupType = getResources().getStringArray(R.array.groupType_options)[groupTypePos];

            groupSize = groupSizeSpinner.getSelectedItemPosition();

            int minSize = 1, maxSize = 1;

            switch(groupSize){
                case 0: // All
                    maxSize = Integer.MAX_VALUE;
                    break;
                case 1: // Small
                    maxSize = 10;
                    break;
                case 2: // Medium
                    minSize = 11;
                    maxSize = 50;
                    break;
                case 3: // Large
                    minSize = 51;
                    maxSize = Integer.MAX_VALUE;
                    break;
                case 4: // Custom
                    minSize = customMin;
                    maxSize = customMax;
                    break;
            }

            RankingLoader loader = new RankingLoader(context, fsVenueId, minSize, maxSize, groupType);
            return loader;
        }

        @Override
        public void onLoaderReset(Loader<List<RankingBean>> loader) {
            //rankingListView.setAdapter(null);
        }


    };


    /**
     *
     * Loader 2: Checkin & Refresh ranking after checkin
     *
     */

    private LoaderManager.LoaderCallbacks<AsyncResult<List<RankingBean>>> mCheckInLoaderListener
            = new LoaderManager.LoaderCallbacks<AsyncResult<List<RankingBean>>>() {

        @Override
        public void onLoadFinished(Loader<AsyncResult<List<RankingBean>>> loader, AsyncResult<List<RankingBean>> result) {
            Log.d("VenueFragment", "onLoadFinished after checkin loader");

            if(result.getException() == null){
                if(result.getData()!=null){
                    ranking=result.getData();
                    mAdapter.setRankings(ranking);
                    mAdapter.notifyDataSetChanged();

                    if( rankingView.getVisibility()==View.INVISIBLE)
                        Toast.makeText(getActivity(), "Congrats! You just scored the first points at this venue.", Toast.LENGTH_SHORT).show();

                    rankingView.setVisibility(View.VISIBLE);
                    noRankingForFilter.setVisibility(View.GONE);
                    noCheckinYet.setVisibility(View.GONE);
                }else{
                    rankingView.setVisibility(View.INVISIBLE);
                    if(groupSize == 0 && groupTypePos == 0){
                        noCheckinYet.setVisibility(View.VISIBLE);
                    }else{
                        noRankingForFilter.setVisibility(View.VISIBLE);
                    }
                }
            } else{
                try{
                    throw result.getException();
                } catch (Exception e) {
                    Log.d(TAG, e.getMessage());
                    if (e instanceof GoogleJsonResponseException){
                        GoogleJsonResponseException ex = (GoogleJsonResponseException) e;
                        switch (ex.getStatusCode()){
                            case 409:
                                List<GoogleJsonError.ErrorInfo> errors = ex.getDetails().getErrors();
                                for(GoogleJsonError.ErrorInfo err : errors){
                                    Log.d(TAG, err.getMessage());
                                    Toast.makeText(context, err.getMessage(), Toast.LENGTH_SHORT).show();

                                }
          /*and the rest of codes available through endpoints*/
                        }
                    } else {
      /*Manage other exceptions, maybe connection issues?*/
                        Toast.makeText(context, context.getString(R.string.error_checkin), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            checkinPending = false;
        }

        @Override
        public Loader<AsyncResult<List<RankingBean>>> onCreateLoader(int id, Bundle args) {
            Log.d("venueFragment", "onCreateLoader");
            checkinPending = true;

            groupTypePos = groupTypeSpinner.getSelectedItemPosition();
            String groupType = getResources().getStringArray(R.array.groupType_options)[groupTypePos];

            groupSize = groupSizeSpinner.getSelectedItemPosition();

            int minSize = 1, maxSize = 1;

            switch(groupSize){
                case 0: // All
                    maxSize = Integer.MAX_VALUE;
                    break;
                case 1: // Small
                    maxSize = 10;
                    break;
                case 2: // Medium
                    minSize = 11;
                    maxSize = 50;
                    break;
                case 3: // Large
                    minSize = 51;
                    maxSize = Integer.MAX_VALUE;
                    break;
                case 4: // Custom
                    minSize = customMin;
                    maxSize = customMax;
                    break;
            }

            CheckInLoader loader = new CheckInLoader(context, fsVenueId, minSize, maxSize, groupType);
            return loader;
        }

        @Override
        public void onLoaderReset(Loader<AsyncResult<List<RankingBean>>> loader) {
            //rankingListView.setAdapter(null);
        }


    };

    private SizeSelectorDialog.SizeSelectorListener mSizeSelectorListener = new SizeSelectorDialog.SizeSelectorListener() {
        @Override
        public void setNewSizes(int min, int max) {
            customMin = min;
            customMax = max;
            getLoaderManager().restartLoader(0, null, mRankingLoaderListener);
        }
    };
}
