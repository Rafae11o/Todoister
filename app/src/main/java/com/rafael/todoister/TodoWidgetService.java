package com.rafael.todoister;

import static com.rafael.todoister.TodoAppWidgetProvider.EXTRA_ITEM_POSITION;

import android.annotation.SuppressLint;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rafael.todoister.model.Task;
import com.rafael.todoister.util.Utils;
import com.squareup.okhttp.internal.DiskLruCache;

import java.util.ArrayList;
import java.util.List;

public class TodoWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new TodoWidgetItemFactory(getApplicationContext(), intent);
    }

    static class TodoWidgetItemFactory implements RemoteViewsFactory{

        private Context context;
        private int appWidgetId;
        private String[] exampleData = {"one", "two", "three", "four", "Example Example Example Example sadf ad Example Example Example Example", "three", "three", "three", "three", "three", "three", "three", "three", "three", "three", "three", "three", "three", "three", "three", "three", "three", "three", "three"};
        public static List<Task> tasks = new ArrayList<>();

        private DatabaseReference reference;

        TodoWidgetItemFactory(Context context, Intent intent) {
            this.context = context;
            this.appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            FirebaseUser user = mAuth.getCurrentUser();
            assert user != null;
            String userId = user.getUid();

            this.reference = FirebaseDatabase.getInstance().getReference().child("Tasks").child(userId);

//            reference.get().addOnCompleteListener()

            this.reference.get().addOnCompleteListener(task -> {
                if(task.isSuccessful()) {
                    for(DataSnapshot snapshot: task.getResult().getChildren()) {
                        tasks.add(snapshot.getValue(Task.class));
                    }
                }
            });

        }

        @Override
        public void onCreate() {

        }

        @Override
        public void onDataSetChanged() {
        }

        @Override
        public void onDestroy() {

        }

        @Override
        public int getCount() {
            return tasks.size();
        }

        @Override
        public RemoteViews getViewAt(int i) {
            @SuppressLint("RemoteViewLayout") RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.todo_widget_row);
            Log.d(this.getClass().toString(), "HERE: " + i);
            views.setTextViewText(R.id.widget_item_text, tasks.get(i).getTask());
            views.setTextViewText(R.id.widget_item_date, Utils.formatDate(tasks.get(i).getDueDate()));
            views.setTextViewText(R.id.widget_item_priority, tasks.get(i).getPriority().getPriority());
            return views;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }
}
