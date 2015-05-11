package be.ugent.vop.ui.venue;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import be.ugent.vop.R;
import be.ugent.vop.utils.RangeSeekBar;


public class SizeSelectorDialog extends DialogFragment {

    private static final Integer MIN_PARTICIPANTS = 1;
    private static final Integer MAX_PARTICIPANTS = 1000;
    private RangeSeekBar<Integer> seekBar;

    public interface SizeSelectorListener{
        public void setNewSizes(int min, int max);
    }

    private SizeSelectorListener mListener;

    public void setListener(SizeSelectorListener listener){
        mListener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        LinearLayout view = (LinearLayout) inflater.inflate(R.layout.dialog_group_size_selector, null);

        final TextView min = (TextView) view.findViewById(R.id.min);
        final TextView max = (TextView) view.findViewById(R.id.max);

        // create RangeSeekBar as Integer range between Min- and Max participants
        seekBar = new RangeSeekBar<>(MIN_PARTICIPANTS, MAX_PARTICIPANTS, getActivity());
        // add RangeSeekBar to pre-defined layout
        view.addView(seekBar);

        builder.setMessage(R.string.select_group_size)
                .setView(view)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (mListener != null) {
                            mListener.setNewSizes(seekBar.getSelectedMinValue(), seekBar.getSelectedMaxValue());
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });

        min.setText(MIN_PARTICIPANTS.toString());
        max.setText(MAX_PARTICIPANTS.toString());

        seekBar.setOnRangeSeekBarChangeListener(new RangeSeekBar.OnRangeSeekBarChangeListener<Integer>() {
            @Override
            public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar, Integer minValue, Integer maxValue) {
                min.setText(minValue.toString());
                max.setText(maxValue.toString());

            }
        });
        // Create the AlertDialog object and return it
        return builder.create();
    }

}
