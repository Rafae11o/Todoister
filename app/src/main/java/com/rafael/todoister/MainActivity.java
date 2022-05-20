package com.rafael.todoister;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.chip.Chip;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

import com.google.firebase.database.DataSnapshot;
import com.rafael.todoister.model.SharedViewModel;
import com.rafael.todoister.model.Task;
import com.rafael.todoister.model.TaskViewModel;
import com.rafael.todoister.util.Utils;


public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;

    private TaskViewModel taskViewModel;

    private SharedViewModel sharedViewModel;

    private ProgressBar progressBar;

    BottomSheetFragment bottomSheetFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = findViewById(R.id.loading_todo_list);
        progressBar.setVisibility(View.VISIBLE);

        if(FirebaseAuth.getInstance().getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bottomSheetFragment = new BottomSheetFragment();
        ConstraintLayout constraintLayout = findViewById(R.id.bottomSheet);
        BottomSheetBehavior<ConstraintLayout> bottomSheetBehavior = BottomSheetBehavior.from(constraintLayout);
        bottomSheetBehavior.setPeekHeight(BottomSheetBehavior.STATE_HIDDEN);

        recyclerView = findViewById(R.id.recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        taskViewModel = new ViewModelProvider
                .AndroidViewModelFactory(MainActivity.this.getApplication())
                .create(TaskViewModel.class);

        sharedViewModel = new ViewModelProvider(this).get(SharedViewModel.class);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> showBottomSheetDialog());
    }

    private void showBottomSheetDialog() {
        bottomSheetFragment.show(getSupportFragmentManager(), bottomSheetFragment.getTag());
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Task> options = new FirebaseRecyclerOptions.Builder<Task>()
                .setQuery(TaskViewModel.getReference(), Task.class)
                .build();

        FirebaseRecyclerAdapter<Task, ViewHolder> adapter = new FirebaseRecyclerAdapter<Task, ViewHolder>(options) {

            @Override
            public void onDataChanged() {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                LinearLayout emptyListMsgGroup = findViewById(R.id.empty_list_msg);
                emptyListMsgGroup.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
            }

            @Override
            protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull Task model) {
                ColorStateList colorStateList = new ColorStateList(
                        new int[][]{
                                new int[] { -android.R.attr.state_enabled },
                                new int[] { android.R.attr.state_enabled },
                        },
                        new int[] {
                                Color.LTGRAY, // disabled
                                Utils.priorityColor(model)
                        }
                );

                holder.task.setText(model.getTask());

                holder.todayChip.setText(Utils.formatDate(model.getDueDate()));
                holder.todayChip.setTextColor(Utils.priorityColor(model));
                holder.todayChip.setChipIconTint(colorStateList);

                holder.radioButton.setButtonTintList(colorStateList);

                holder.priorityChip.setText(model.getPriority().getPriority());
                holder.priorityChip.setTextColor(Utils.priorityColor(model));
                holder.priorityChip.setChipIconTint(colorStateList);

                holder.radioButton.setOnClickListener(view -> onTodoRadioButtonClick(model));

                holder.getView().setOnClickListener(view -> onTodoClick(model));
            }

            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.todo_row, parent, false);
                return new ViewHolder(view);
            }
        };

        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        public AppCompatRadioButton radioButton;
        public AppCompatTextView task;
        public Chip todayChip;
        public Chip priorityChip;

        private final View view;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            radioButton = view.findViewById(R.id.todo_radio_button);
            task = view.findViewById(R.id.todo_row_todo);
            todayChip = view.findViewById(R.id.todo_row_date_chip);
            priorityChip = view.findViewById(R.id.todo_row_priority_chip);
        }

        public View getView() {
            return view;
        }
    }

    private void onTodoClick(@NonNull Task task) {
        Log.d(this.getClass().toString(), "On todo: " + task);
        sharedViewModel.selectItem(task);
        sharedViewModel.setEdit(true);
        showBottomSheetDialog();
    }

    private void onTodoRadioButtonClick(@NonNull Task task) {
        Log.d(this.getClass().toString(), "Radio btn: " + task);
        TaskViewModel.delete(task.getId());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.logout) {
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            mAuth.signOut();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

}