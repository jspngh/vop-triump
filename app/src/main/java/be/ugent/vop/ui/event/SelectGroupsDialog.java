package be.ugent.vop.ui.event;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import be.ugent.vop.R;
import be.ugent.vop.backend.myApi.model.GroupBean;

/**
 * Created by vincent on 26/03/15.
 */
public class SelectGroupsDialog extends DialogFragment {
    private static final String TAG = "SelectGroupsDialog";

    List<GroupBean> selectedGroups;
    List<GroupBean> userGroups;

    ListView groupsListView;

    NewEventGroupListAdapter adapter;

    SelectGroupsDialogListener listener;

    public interface SelectGroupsDialogListener {
        public void onDialogPositiveClick(DialogFragment dialog);
        public void onDialogNegativeClick(DialogFragment dialog);
    }

    public void setSelectGroupsDialogListener(SelectGroupsDialogListener listener){
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            this.listener = (SelectGroupsDialogListener) listener;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(listener.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    public void setGroups(List<GroupBean> userGroups){
        this.userGroups = userGroups;
    }

    public List<GroupBean> getSelectedGroups(){
        return this.selectedGroups;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View rootView = inflater.inflate(R.layout.dialog_select_groups, null);
        groupsListView = (ListView) rootView.findViewById(R.id.listViewSelectGroups);

        builder.setView(rootView)
                .setTitle(getString(R.string.new_event_select_groups_dialog_title))
                .setPositiveButton(R.string.button_text_set, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        fillSelectedGroups();
                        listener.onDialogPositiveClick(SelectGroupsDialog.this);
                    }
                })
                .setNegativeButton(R.string.button_text_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        listener.onDialogPositiveClick(SelectGroupsDialog.this);

                    }
                });
        // Create the AlertDialog object and return it

        if(userGroups!=null){
            adapter = new NewEventGroupListAdapter(getActivity(),userGroups);
            if(selectedGroups!=null) adapter.setCheckedGroupBeans(selectedGroups);
            groupsListView.setAdapter(adapter);


        }else{
            Toast.makeText(getActivity(), "No groups available", Toast.LENGTH_SHORT)
                    .show();
        }

        return builder.create();
    }


    private void fillSelectedGroups(){
        selectedGroups = new ArrayList<GroupBean>();
        for(int i = 0;i<groupsListView.getChildCount();i++)
        {
            View view = groupsListView.getChildAt(i);
            CheckedTextView cv =(CheckedTextView)view.findViewById(R.id.checkedTextViewGroup);
            if(cv.isChecked())
            {
                selectedGroups.add(adapter.getItem(i));
            }
        }
    }


}


