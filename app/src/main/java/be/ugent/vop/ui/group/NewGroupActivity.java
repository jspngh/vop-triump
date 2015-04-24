package be.ugent.vop.ui.group;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.gc.materialdesign.views.ButtonRectangle;

import be.ugent.vop.R;
import be.ugent.vop.backend.loaders.CreateGroupLoader;
import be.ugent.vop.backend.myApi.model.GroupBean;
import be.ugent.vop.utils.PrefUtils;

public class NewGroupActivity extends ActionBarActivity {

    private EditText name;
    private EditText description;
    private ButtonRectangle btn;
    private Spinner groupTypeSpinner;

    private Context mContext;
    private Toolbar mActionBarToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        boolean darkTheme = PrefUtils.getDarkTheme(this);
        if(darkTheme) setTheme(R.style.AppTheme_Dark);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_group);
        getActionBarToolbar();

        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
            ab.setTitle(null);
        }

        mContext = getApplicationContext();

        description = (EditText) findViewById(R.id.group_description_edit);
        name = (EditText) findViewById(R.id.group_name_edit);
        btn = (ButtonRectangle) findViewById(R.id.add_button);
        btn.setFocusable(false);


        groupTypeSpinner = (Spinner) findViewById(R.id.spinnerGroupType);
        ArrayAdapter<CharSequence> adapterType = ArrayAdapter.createFromResource(this,
                R.array.groupType_spinner_options, android.R.layout.simple_spinner_item);
        adapterType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        groupTypeSpinner.setAdapter(adapterType);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLoaderManager().initLoader(1, null, mCreateGroupLoaderListener);
            }
        });

    }

    private Toolbar getActionBarToolbar() {
        if (mActionBarToolbar == null) {
            mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
            if (mActionBarToolbar != null) {
                setSupportActionBar(mActionBarToolbar);
            }
        }
        return mActionBarToolbar;
    }

    private void saveFinished(GroupBean response){
        if (response != null){
            btn.setVisibility(View.GONE);
            //     result.setVisibility(View.VISIBLE);
            Toast.makeText(this, getString(R.string.new_group_success), Toast.LENGTH_SHORT).show();
            finish();
            Log.d("GroupNewFragment", "group added successfully");
        } else {
            Toast.makeText(this, getString(R.string.new_group_failed), Toast.LENGTH_SHORT).show();
        }
    }

    private LoaderManager.LoaderCallbacks<GroupBean> mCreateGroupLoaderListener
            = new LoaderManager.LoaderCallbacks<GroupBean>() {
        @Override
        public void onLoadFinished(Loader<GroupBean> loader, GroupBean response) {
            saveFinished(response);
        }

        @Override
        public Loader<GroupBean> onCreateLoader (int id, Bundle args){
            String groupType = groupTypeSpinner.getSelectedItem().toString();
            return new CreateGroupLoader(mContext, name.getText().toString(), description.getText().toString(), groupType);
        }

        @Override
        public void onLoaderReset(Loader<GroupBean> loader) {

        }
    };


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
