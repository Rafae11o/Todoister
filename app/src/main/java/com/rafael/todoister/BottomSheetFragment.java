package com.rafael.todoister;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.Group;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.snackbar.Snackbar;
import com.rafael.todoister.model.Priority;
import com.rafael.todoister.model.SharedViewModel;
import com.rafael.todoister.model.Task;
import com.rafael.todoister.model.TaskViewModel;
import com.rafael.todoister.util.Utils;

import java.util.Calendar;
import java.util.Date;

public class BottomSheetFragment extends BottomSheetDialogFragment implements View.OnClickListener {

    private EditText enterTodo;
    private Button calendarButton;
    private Button priorityButton;
    private RadioGroup priorityRadioGroup;
    private RadioButton selectedRadioButton;
    private int selectedButtonId;
    private ImageButton saveButton;
    private CalendarView calendarView;
    private LinearLayout calendarGroup;

    private final Calendar calendar = Calendar.getInstance();

    private Date dueDate;
    private Priority priority;
    private boolean isEdit;

    private SharedViewModel sharedViewModel;

    public BottomSheetFragment() { }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet, container, false);
        calendarGroup = view.findViewById(R.id.calendar_group);
        calendarView = view.findViewById(R.id.calendar_view);
        calendarButton = view.findViewById(R.id.today_calendar_button);
        enterTodo = view.findViewById(R.id.enter_todo_et);
        saveButton = view.findViewById(R.id.save_todo_button);
        priorityButton = view.findViewById(R.id.priority_todo_button);
        priorityRadioGroup = view.findViewById(R.id.radioGroup_priority);

        Chip todayChip = view.findViewById(R.id.today_chip);
        todayChip.setOnClickListener(this);

        Chip tomorrowChip = view.findViewById(R.id.tomorrow_chip);
        tomorrowChip.setOnClickListener(this);

        Chip nextWeekChip = view.findViewById(R.id.next_week_chip);
        nextWeekChip.setOnClickListener(this);

        calendar.add(Calendar.DAY_OF_YEAR, 0);
        setDueDate(calendar.getTime());

        setPriorityWithRadioButton(Priority.LOW);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if(sharedViewModel.getSelectedItem().getValue() != null) {
            isEdit = sharedViewModel.isEdit();
            Task task = sharedViewModel.getSelectedItem().getValue();
            enterTodo.setText(task.getTask());
            setDueDate(task.getDueDate());
            setPriorityWithRadioButton(task.getPriority());
            Log.d(this.getClass().toString(),  task.toString());
        }
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        calendarButton.setOnClickListener(view1 -> {
            calendarGroup.setVisibility(
                    calendarGroup.getVisibility() == View.GONE ? View.VISIBLE : View.GONE
            );
            Utils.hideSoftKeyboard(view1);
        });

        calendarView.setOnDateChangeListener((calendarView, year, month, day) -> {
            calendar.clear();
            calendar.set(year, month, day);
            setDueDate(calendar.getTime());
        });

        priorityButton.setOnClickListener(view1 -> {
            Utils.hideSoftKeyboard(view);
            priorityRadioGroup.setVisibility(
                    priorityRadioGroup.getVisibility() == View.GONE ? View.VISIBLE : View.GONE
            );
            priorityRadioGroup.setOnCheckedChangeListener((radioGroup, checkedId) -> {
                if(priorityRadioGroup.getVisibility() == View.VISIBLE) {
                    selectedButtonId = checkedId;
                    selectedRadioButton = view.findViewById(selectedButtonId);
                    if(selectedRadioButton.getId() == R.id.radioButton_high) {
                        setPriority(Priority.HIGH);
                    } else if(selectedRadioButton.getId() == R.id.radioButton_med) {
                        setPriority(Priority.MEDIUM);
                    } else if(selectedRadioButton.getId() == R.id.radioButton_low) {
                        setPriority(Priority.LOW);
                    } else {
                        setPriority(Priority.LOW);
                    }
                } else {
                    setPriority(Priority.LOW);
                }
            });
        });

        saveButton.setOnClickListener(view1 -> {
            String task = enterTodo.getText().toString().trim();
            if(TextUtils.isEmpty(task)) {
                enterTodo.setError("Empty task");Snackbar.make(saveButton, R.string.something_wrong_with_due_date_or_priority, Snackbar.LENGTH_LONG).show();
                Snackbar.make(view1, R.string.something_wrong_with_due_date_or_priority, Snackbar.LENGTH_LONG).show();
                return;
            }
            if(dueDate == null || priority == null) {
                Snackbar.make(saveButton, R.string.something_wrong_with_due_date_or_priority, Snackbar.LENGTH_LONG).show();
                return;
            }
            if(isEdit) {
                Task updateTask = sharedViewModel.getSelectedItem().getValue();
                if (updateTask != null) {
                    updateTask.setTask(task);
                    updateTask.setDueDate(dueDate);
                    updateTask.setPriority(priority);
                    TaskViewModel.update(updateTask).addOnCompleteListener(task1 -> {
                        if(task1.isSuccessful()) {
                            Snackbar.make(saveButton, R.string.updated_successfully, Snackbar.LENGTH_LONG).show();
                            enterTodo.setText("");
                            if(this.isVisible()) {
                                this.dismiss();
                            }
                        } else {
                            Snackbar.make(saveButton, R.string.something_went_wrong, Snackbar.LENGTH_LONG).show();
                        }
                    });
                }
                return;
            }
            Task mTask = new Task(
                    task,
                    priority,
                    dueDate,
                    Calendar.getInstance().getTime(),
                    false
            );
            TaskViewModel.save(mTask).addOnCompleteListener(task1 -> {
                if(task1.isSuccessful()) {
                    enterTodo.setText("");
                    setPriority(Priority.LOW);
                    calendar.setTime(new Date());
                    calendar.add(Calendar.DAY_OF_YEAR, 0);
                    setDueDate(calendar.getTime());
                    Snackbar.make(saveButton, R.string.saved_successfully, Snackbar.LENGTH_LONG).show();
                    if(this.isVisible()) {
                        this.dismiss();
                    }
                } else {
                    Snackbar.make(saveButton, R.string.something_went_wrong, Snackbar.LENGTH_LONG).show();
                }
            });
        });

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        calendar.setTime(new Date());
        if(id == R.id.today_chip) {
            calendar.add(Calendar.DAY_OF_YEAR, 0);
            setDueDate(calendar.getTime());
        } else if(id == R.id.tomorrow_chip) {
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            setDueDate(calendar.getTime());
        } else if(id == R.id.next_week_chip) {
            calendar.add(Calendar.DAY_OF_YEAR, 7);
            setDueDate(calendar.getTime());
        }
    }

    private void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
        calendarButton.setText(Utils.formatDate(dueDate));
    }

    private void setPriority(@NonNull Priority priority) {
        this.priority = priority;
        priorityButton.setText(priority.getPriority());
    }

    private void setPriorityWithRadioButton(Priority priority) {
        setPriority(priority);
        if(priority == Priority.HIGH) {
            priorityRadioGroup.check(R.id.radioButton_high);
        } else if (priority == Priority.MEDIUM) {
            priorityRadioGroup.check(R.id.radioButton_med);
        } else {
            priorityRadioGroup.check(R.id.radioButton_low);
        }
    }
}
