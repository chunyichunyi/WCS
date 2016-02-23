package com.example.admin.wcs;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class Fragment3 extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment3_main,container,false);

        TextView txt_content = (TextView) view.findViewById(R.id.txt_content);
        txt_content.setText("hello world");
        return view;
    }
}
