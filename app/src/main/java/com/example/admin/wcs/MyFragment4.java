package com.example.admin.wcs;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MyFragment4 extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_fragment,container,false);

        TextView txt_content = (TextView) view.findViewById(R.id.txt_content);
        txt_content.setText("Wireless Control System 0.2.10\n" +
                            "built on January 15, 2016");
        return view;
    }
}
