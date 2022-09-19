package com.example.hourgate.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hourgate.models.ReportModel;
import com.example.hourgate.R;

import java.util.ArrayList;

/*
The following course helped with understanding the process of creating some of the standard adapter class methods
in the context of this application:
https://www.udemy.com/course/full-android-11-masterclass-course-with-java-53-hours/
*/

public class ReportAdapter extends RecyclerView.Adapter<ReportAdapter.EmployeeViewHolder> {

    private ArrayList<ReportModel> reports ;

    public ReportAdapter(ArrayList<ReportModel> reportsArray) {
        reports = reportsArray;
    }


    // defining the reports layout/design
    @NonNull
    @Override
    public EmployeeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_report,parent,false);
        return new EmployeeViewHolder(view);
    }


    // method called by our Reports View to display the reports data at the specified positions.
    @Override
    public void onBindViewHolder(@NonNull EmployeeViewHolder holder, int position) {
        ReportModel reportModel = reports.get(position);
        holder.name.setText(reportModel.getName());
        holder.wages.setText(reportModel.getTotalWages()+"");
        holder.hours.setText(reportModel.getHourWorked()+"");
    }

    // returning the size of the array list.
    @Override
    public int getItemCount() {
        Log.e("reportsAdapter",reports.size()+"");
        return reports.size();
    }

    public static class EmployeeViewHolder extends RecyclerView.ViewHolder{

        // creating variables for our views.
        TextView name,hours,wages;
        // initializing our views with their ids.
        public EmployeeViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.name);
            wages = itemView.findViewById(R.id.wages);
            hours = itemView.findViewById(R.id.hours);
        }
    }
}
