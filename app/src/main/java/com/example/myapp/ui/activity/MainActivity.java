package com.example.myapp.ui.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapp.AppDatabase;
import com.example.myapp.User;
import com.example.myapp.databinding.ActivityMainBinding;
import java.util.List;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        
        // Initialize Database
        db = AppDatabase.getDatabase(this);

        // Load existing database records onto the screen right away
        loadAndDisplayUsers();

        // Handle Button Click
        binding.buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String firstName = binding.etFirstName.getText().toString().trim();
                String lastName = binding.etLastName.getText().toString().trim();

                // Simple Validation Check
                if (firstName.isEmpty() || lastName.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please enter both names", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Database operation on background thread
                Executors.newSingleThreadExecutor().execute(new Runnable() {
                    @Override
                    public void run() {
                        // 1. Insert input data into DB
                        User newUser = new User(firstName, lastName);
                        db.userDao().insert(newUser);

                        // 2. Fetch updated records and refresh UI
                        loadAndDisplayUsers();
                        
                        // Clear the input fields on the UI thread for convenience
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                binding.etFirstName.setText("");
                                binding.etLastName.setText("");
                                Toast.makeText(MainActivity.this, "Saved!", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
        });
    }

    /**
     * Helper method to grab all records from Room and render them onto the TextView
     */
    private void loadAndDisplayUsers() {
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                // Fetch the list from Database
                List<User> userList = db.userDao().getAllUsers();

                // Format the string block
                StringBuilder builder = new StringBuilder();
                for (User u : userList) {
                    builder.append("ID: ").append(u.id)
                           .append(" | ").append(u.firstName)
                           .append(" ").append(u.lastName)
                           .append("\n\n"); // Two line breaks for neat spacing
                }

                final String finalDisplayData = builder.toString();

                // Push formatted string to the UI Thread TextView
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (finalDisplayData.isEmpty()) {
                            binding.tvDisplayUsers.setText("No users in database yet.");
                        } else {
                            binding.tvDisplayUsers.setText(finalDisplayData);
                        }
                    }
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}