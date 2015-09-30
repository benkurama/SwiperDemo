package com.example.swiperdemo.fragments;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class ProgressDialogFragment extends DialogFragment {
    public static ProgressDialogFragment newInstance(String title, String msg) {
        ProgressDialogFragment fragment = new ProgressDialogFragment();

        Bundle args = new Bundle();
        args.putString("msgId", msg);
        args.putString("title", title);

        fragment.setArguments(args);

        return fragment;
   }

    public ProgressDialogFragment() {
        // Empty constructor required for DialogFragment
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String msgId = getArguments().getString("msgId");
        String title = getArguments().getString("title");
        ProgressDialog dialog = new ProgressDialog(getActivity());
        
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setMessage(msgId);
        dialog.setTitle(title);
        
        return dialog;
    }
    
    public void setMessage(String msg){
    	 new ProgressDialog(getActivity()).setMessage(msg);
    }
}
