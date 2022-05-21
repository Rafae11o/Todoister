package com.rafael.todoister.model;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class TaskViewModel extends AndroidViewModel {

    private static DatabaseReference reference;

    private static List<Task> tasks;

    public TaskViewModel(@NonNull Application application) {
        super(application);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        assert user != null;
        String userId = user.getUid();

        reference = FirebaseDatabase.getInstance().getReference().child("Tasks").child(userId);

    }

    public static com.google.android.gms.tasks.Task<Void> save(Task task) {
        task.setId(reference.push().getKey());
        return reference.child(task.getId()).setValue(task);
    }

    public static com.google.android.gms.tasks.Task<Void> update(Task task) {
        return reference.child(task.getId()).setValue(task);
    }

    public static com.google.android.gms.tasks.Task<Void> delete(String key) {
        return reference.child(key).removeValue();
    }

    public static DatabaseReference getReference() {
        return reference;
    }

}
