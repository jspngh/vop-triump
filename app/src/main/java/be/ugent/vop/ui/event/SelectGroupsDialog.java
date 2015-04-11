package be.ugent.vop.ui.event;


import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import be.ugent.vop.R;
import be.ugent.vop.backend.myApi.model.GroupBean;

/**
 * Created by vincent on 26/03/15.
 */
public class SelectGroupsDialog extends DialogFragment implements AdapterView.OnItemClickListener {

    List<GroupBean> selectedGroups = new ArrayList<>();
    List<GroupBean> userGroups = new ArrayList<>();
    List<Long> previouslySelectedGroups = null;

    ListView groupsListView;
    Button cancelButton;
    Button setButton;

    NewEventGroupListAdapter adapter;
    Context context;

    public void setGroups(List<GroupBean> userGroups){
        this.userGroups = userGroups;
    }

    public void setContext(Context c){
        this.context =c;
    }

    public void setSelectedGroups(List<GroupBean> s){
        this.selectedGroups = s;
    }
/*
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();

        builder.setView(inflater.inflate(R.layout.dialog_select_groups, null))
                .setMessage(getString(R.string.new_event_select_groups_dialog_title))
                .setPositiveButton(R.string.button_text_set, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        fillSelectedGroups();
                        previouslySelectedGroups = new ArrayList<Long>();
                        for(GroupBean g:selectedGroups){
                            previouslySelectedGroups.add(g.getGroupId());
                        }
                        dismiss();
                    }
                })
                .setNegativeButton(R.string.button_text_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        dismiss();
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
*/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().setTitle(R.string.event_select_groups_dialog_title);

        View view = inflater.inflate(R.layout.dialog_select_groups, null, false);
        groupsListView = (ListView) view.findViewById(R.id.listViewSelectGroups);

        setButton = (Button) view.findViewById(R.id.buttonSet);
        cancelButton = (Button) view.findViewById(R.id.buttonCancel);
        initButtons();



        if(userGroups!=null && context!=null){
            adapter = new NewEventGroupListAdapter(context,userGroups);
            groupsListView.setAdapter(adapter);
            groupsListView.setOnItemClickListener(this);

            if(previouslySelectedGroups!=null){
                setPreviouslySelected();
            }
        }else{
            Toast.makeText(getActivity(), "No groups available", Toast.LENGTH_SHORT)
                    .show();
        }

        return view;
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,
                            long id) {

    }

    private void initButtons(){

        setButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fillSelectedGroups();
                previouslySelectedGroups = new ArrayList<Long>();
                for(GroupBean g:selectedGroups){
                    previouslySelectedGroups.add(g.getGroupId());
                }
                dismiss();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                   dismiss();
            }
        });
    }

    private void fillSelectedGroups(){
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

    private void setPreviouslySelected(){
        for(int i = 0;i<groupsListView.getChildCount();i++)
        {
            View view = groupsListView.getChildAt(i);
            CheckedTextView cv =(CheckedTextView)view.findViewById(R.id.checkedTextViewGroup);
            if(previouslySelectedGroups.contains(userGroups.get(i).getGroupId())){
                cv.setChecked(true);
            }else cv.setChecked(false);
        }
    }

}


