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

import com.example.hourgate.models.UserModel;
import com.example.hourgate.R;

import java.util.ArrayList;


/*
The following course helped with understanding the process of creating some of the standard adapter class methods
in the context of this application:
https://www.udemy.com/course/full-android-11-masterclass-course-with-java-53-hours/
*/

public class EmployeesAdapter extends RecyclerView.Adapter<EmployeesAdapter.EmployeeViewHolder> {

    public static EmployeeActionListener _employeeActionListener;
    private ArrayList<UserModel> employeesList;

    // creating a constructor for our adapter. It requires the arraylist of type usermodel and the EmployeeActionListener interface (see bottom of the class)
    public EmployeesAdapter (ArrayList<UserModel> employeesList, EmployeesAdapter.EmployeeActionListener employeeActionListener) {
        this.employeesList = employeesList;
        _employeeActionListener = employeeActionListener;
    }

    // method for filtering our recyclerview items.
    public void filterList(ArrayList<UserModel> filterllist) {
        // to add our filtered list to our employees array list.
        employeesList = filterllist;
        // to notify our adapter that data changes have occurred in the recycler view.
        notifyDataSetChanged();
    }

    // Called when RecyclerView needs a new RecyclerView.ViewHolder of the given type to represent an item
    // here we are defining/linking the employees card design to each item in the recyclerview
    @NonNull
    @Override
    public EmployeeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.employees_layout_background,parent,false);
        return new EmployeeViewHolder(view);
    }

    // method called by our Employee RecyclerView to display the employees data at the specified positions.
    @Override
    public void onBindViewHolder(@NonNull EmployeeViewHolder holder, int position) {
        // get the position of the UserModel (employee)
        UserModel employeesModel = employeesList.get(position);
        // set the name of the UserModel (employee) using .getName()
        holder.Tv_Employee_Name.setText(employeesModel.getName());

        // click listener for the delete employee button
        holder.Btn_Delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // getting the delete employee dialog and then linking the dialog buttons to our variables
                Dialog dialog = new Dialog(view.getRootView().getContext());
                View dialogView = LayoutInflater.from(view.getRootView().getContext()).inflate(R.layout.delete_employee_dialog_layout,null);
                dialog.setContentView(dialogView);

                Button Btn_Cancel,Btn_Delete;
                Btn_Cancel = dialog.findViewById(R.id.Btn_cancel_DeleteDialog);
                Btn_Delete = dialog.findViewById(R.id.Btn_delete_DeleteDialog);

                // click listener for the cancel button in the delete employee dialog
                Btn_Cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(dialog.getContext(), "Cancelled", Toast.LENGTH_SHORT).show();
                        dialog.dismiss(); // close the dialog
                    }
                });
                // click listener for the delete button in the delete employee dialog
                Btn_Delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss(); // close the dialog
                        // on the delete button call being call the onDeleteButtonClicked method for that employee
                        _employeeActionListener.onDeleteButtonClicked(employeesModel); // call the delete onDeleteButtonClicked
                    }
                });
                dialog.setCancelable(false);
                dialog.show(); // to ensure dialog is actually shown on the screen
            }
        });

        // click listener for when any item (employee) is clicked
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // on a employee being clicked invoke the onEmployeeClicked method for that employee
                _employeeActionListener.onEmployeeClicked(employeesModel);
            }
        });
    }


    // returning the size of the array list.
    @Override
    public int getItemCount() {
        return employeesList.size();
    }


    public static class EmployeeViewHolder extends RecyclerView.ViewHolder{
        // creating variables for our views.
        TextView Tv_Employee_Name;
        Button Btn_Delete;

        public EmployeeViewHolder(@NonNull View itemView) {
            super(itemView);
            // initializing our views with their ids.
            Tv_Employee_Name = itemView.findViewById(R.id.Tv_Employee_Name);
            Btn_Delete = itemView.findViewById(R.id.Employees_Delete_Button);
        }
    }

    /*
    Adapted in the context of this application from
    https://stackoverflow.com/questions/994840/how-to-create-our-own-listener-interface-in-android/18585247#18585247
    a interface - any related class must implement onDeleteButtonClicked and OnEmployeeClicked methods
    */
    public interface EmployeeActionListener {
        public void onDeleteButtonClicked(UserModel userModel);
        public void onEmployeeClicked(UserModel userModel);
    }
}
