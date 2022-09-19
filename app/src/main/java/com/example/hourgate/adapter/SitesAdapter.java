package com.example.hourgate.adapter;

import android.app.Dialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.hourgate.models.SitesModel;
import com.example.hourgate.R;

import java.util.ArrayList;


/*
The following course helped with understanding the process of creating some of the standard adapter class methods
in the context of this application:
https://www.udemy.com/course/full-android-11-masterclass-course-with-java-53-hours/
*/

public class SitesAdapter extends RecyclerView.Adapter<SitesAdapter.SitesViewHolder> {

    // creating a variable for our array list and context.
    private ArrayList<SitesModel> sitesModelArrayList;
    private SiteActionListener siteActionListener;

    // creating a constructor for our variables.
    public SitesAdapter(ArrayList<SitesModel> courseModalArrayList, SiteActionListener siteActionListener) {
        this.sitesModelArrayList = courseModalArrayList;
        this.siteActionListener = siteActionListener;
    }

    // method for filtering our recyclerview items.
    public void filterList(ArrayList<SitesModel> filterllist) {
        // to add our filtered list to our sites array list.
        sitesModelArrayList = filterllist;
        // to notify our adapter as data changes occur in the recycler view.
        notifyDataSetChanged();
    }

    // defining the Sites card design
    @NonNull
    @Override
    public SitesAdapter.SitesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // to inflate our layout.
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.sites_layout_background, parent, false);
        return new SitesViewHolder(view);
    }

    // method called by our Sites RecyclerView to display the sites data at the specified positions.
    @Override
    public void onBindViewHolder(@NonNull SitesAdapter.SitesViewHolder holder, int position) {
        // setting data to our recycler view components
        SitesModel sitesModel = sitesModelArrayList.get(position);
        holder.Site_Name.setText(sitesModel.getName());
        // click listener for the delete sites button
        holder.Btn_Delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // getting the delete site dialog and linking the dialog buttons to our variables
                Dialog dialog = new Dialog(view.getRootView().getContext());
                View dialogView = LayoutInflater.from(view.getRootView().getContext()).inflate(R.layout.delete_sites_dialog_layout,null);
                dialog.setContentView(dialogView);

                Button Btn_Cancel,Btn_Delete;
                Btn_Cancel = dialog.findViewById(R.id.Btn_cancel_SiteDialog);
                Btn_Delete = dialog.findViewById(R.id.Btn_delete_SiteDialog);
                // click listener for the cancel button in the delete sites dialog
                Btn_Cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(dialog.getContext(), "Cancelled", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });
                // click listener for the delete button in the delete sites dialog
                Btn_Delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        siteActionListener.siteDeleteClicked(sitesModel);
                        dialog.dismiss();

                    }
                });
                dialog.setCancelable(false);
                dialog.show();
            }
        });

        // click listener for when any item (site) is clicked
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                siteActionListener.siteOpenClicked(sitesModel);
            }
        });
    }

    // returning the size of the array list.
    @Override
    public int getItemCount() { return sitesModelArrayList.size(); }

    public class SitesViewHolder extends RecyclerView.ViewHolder {

        // creating variables for our views.
        TextView Site_Name;
        Button Btn_Delete;

        public SitesViewHolder(@NonNull View itemView) {
            super(itemView);
            // initializing our views with their ids.
            Site_Name = itemView.findViewById(R.id.Tv_Site_Name);
            Btn_Delete = itemView.findViewById(R.id.Sites_Delete_Button);
        }
    }

    // https://stackoverflow.com/questions/994840/how-to-create-our-own-listener-interface-in-android/18585247#18585247
    // interface - any related class must implement onDeleteClicked and SiteOpenClicked methods
    public interface SiteActionListener {
        void siteDeleteClicked(SitesModel sitesModel);
        void siteOpenClicked(SitesModel sitesModel);
    }
}
