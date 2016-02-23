package com.example.admin.wcs;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class Fragment4 extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment4_main,container,false);

        TextView textContent = (TextView) view.findViewById(R.id.text_content);
        textContent.setText(R.string.app_version);

        Button buttonLogout = (Button)view.findViewById(R.id.button_logout);

        buttonLogout.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ((MainActivity) getActivity()).mMyApplication.UserLogout();  //退出登录
            }
        });

        if (((MainActivity)getActivity()).mMyApplication.GetDebugFlag()) {
            buttonLogout.setEnabled(false);
        }

        return view;
    }
}
