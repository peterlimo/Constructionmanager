package com.heron.constructmanager.activities.forms;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.heron.constructmanager.animations.LoadingAnimation;
import com.heron.constructmanager.R;
import com.heron.constructmanager.ValidateInput;
import com.heron.constructmanager.models.Responsability;
import com.heron.constructmanager.models.User;
import com.heron.constructmanager.service.ConstructionService;
import com.heron.constructmanager.service.UserService;
import com.hootsuite.nachos.NachoTextView;
import com.hootsuite.nachos.chip.Chip;
import com.hootsuite.nachos.terminator.ChipTerminatorHandler;

import java.util.ArrayList;
import java.util.List;

public class ConstructionPrepFormActivity extends AppCompatActivity {

    NachoTextView nachoTextView;
    ImageView backArrowImg, deleteImg, newStageImg, cancelImg;
    EditText titleEditText, addressEditText, typeEditText;
    Button addButton;

    List<User> selectedUsersList;
    List<User> allUsersList;
    List<String> selectedEmailsList;
    List<String> allEmailsList;

    FirebaseAuth auth;
    FirebaseUser user;
    ConstructionService service;
    UserService userService;

    String userIdStr, titleStr, addressStr, stageStr, typeStr, responsiblesStr, constructionUidStr;

    ValidateInput validateInput;
    LoadingAnimation loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_construction_prep_form);
        constructionUidStr = null;
        stageStr = "Preparação";
        service = new ConstructionService(this);
        userService = new UserService(this);
        selectedEmailsList = new ArrayList();
        allEmailsList = new ArrayList();
        allUsersList = new ArrayList();
        selectedUsersList = new ArrayList();

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        userIdStr = user.getUid();

        // Components
        titleEditText = findViewById(R.id.construction_prep_title);
        addressEditText = findViewById(R.id.construction_prep_address);
        typeEditText = findViewById(R.id.construction_prep_type);
        addButton = findViewById(R.id.construction_prep_add_button);
        backArrowImg = findViewById(R.id.construction_prep_back_arrow);
        nachoTextView = findViewById(R.id.construction_prep_nacho_res_text_view);

        if(getIntent().getExtras() != null) {
            constructionUidStr = getIntent().getStringExtra("constructionUid");
            titleStr = getIntent().getStringExtra("title");
            addressStr = getIntent().getStringExtra("address");
            typeStr = getIntent().getStringExtra("type");
        }

        titleEditText.setText(titleStr);
        addressEditText.setText(addressStr);
        typeEditText.setText(typeStr);

        // Validate
        validateInput = new ValidateInput(ConstructionPrepFormActivity.this, titleEditText, addressEditText, typeEditText, nachoTextView);
        // Loading animation
        loading = new LoadingAnimation(this);
        setUpNachoTextView();

        // TODO achar maneira de preencher os chips nas edições
        DatabaseReference usersReference = userService.getUsersReference();
        usersReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot childSnapshot: dataSnapshot.getChildren()) {
                    User user = childSnapshot.getValue(User.class);
                    user.setUid(childSnapshot.getKey());
                    String email = childSnapshot.child("email").getValue(String.class);
                    allUsersList.add(user);
                    allEmailsList.add(email);
                }
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(ConstructionPrepFormActivity.this, android.R.layout.simple_dropdown_item_1line, allEmailsList);
                nachoTextView.setAdapter(arrayAdapter);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });

        setUpNachoTextView();

        // Listeners
        backArrowImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loading.loadingAnimationDialog();
                if (infosVerified()) {
                    getEditTextsContent();
                    selectedUsersList = userService.getUsersByEmails(selectedEmailsList, allUsersList);
                    service.writeConstructionInfo(userIdStr, titleStr, addressStr, stageStr, typeStr, selectedUsersList, constructionUidStr);
                    loading.dismissLoading();
                    finish();
                }
                else {
                    loading.dismissLoading();
                }
            }
        });

//        deleteImg.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                AlertDialog.Builder builder = new AlertDialog.Builder(ConstructionPrepFormActivity.this);
//                builder.setTitle("Deletar obra");
//                builder.setMessage("Tem certeza que deseja deletar a obra?");
//                AlertDialog dialog = builder.create();
//                dialog.setButton(Dialog.BUTTON_POSITIVE, "Sim", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        selectedUsersList = userService.getUsersByEmails(selectedEmailsList, allUsersList);
//                        service.deleteConstruction(userIdStr, constructionUidStr);
//                        finish();
//                    }
//                });
//                dialog.setButton(Dialog.BUTTON_NEGATIVE, "Não", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//                    }
//                });
//                dialog.show();
//            }
//        });
//
//        cancelImg.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (infosVerified()) {
//                    AlertDialog.Builder builder = new AlertDialog.Builder(ConstructionPrepFormActivity.this);
//                    builder.setTitle("Cancelar obra");
//                    builder.setMessage("Tem certeza que deseja cancelar a obra?");
//                    AlertDialog dialog = builder.create();
//                    dialog.setButton(Dialog.BUTTON_POSITIVE, "Sim", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            getEditTextsContent();
//                            selectedUsersList = userService.getUsersByEmails(selectedEmailsList, allUsersList);
//                            service.cancelConstruction(userIdStr, titleStr, addressStr, typeStr, selectedUsersList, constructionUidStr);
//                            finish();
//                        }
//                    });
//                    dialog.setButton(Dialog.BUTTON_NEGATIVE, "Não", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            dialog.dismiss();
//                        }
//                    });
//                    dialog.show();
//                }
//            }
//        });
//
//        newStageImg.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (infosVerified()) {
//                    AlertDialog.Builder builder = new AlertDialog.Builder(ConstructionPrepFormActivity.this);
//                    builder.setTitle("Avançar etapa");
//                    builder.setMessage("Tem certeza que deseja avançar a etapa da obra para \"Em execução\"?");
//                    AlertDialog dialog = builder.create();
//                    dialog.setButton(Dialog.BUTTON_POSITIVE, "Sim", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            getEditTextsContent();
//                            selectedUsersList = userService.getUsersByEmails(selectedEmailsList, allUsersList);
//                            service.advanceStageToExec(userIdStr, titleStr, addressStr, typeStr, selectedUsersList, constructionUidStr);
//                            finish();
//                        }
//                    });
//                    dialog.setButton(Dialog.BUTTON_NEGATIVE, "Não", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            dialog.dismiss();
//                        }
//                    });
//                    dialog.show();
//
//                }
//            }
//        });

    }

    private void setUpNachoTextView() {
        nachoTextView.addChipTerminator('\n', ChipTerminatorHandler.BEHAVIOR_CHIPIFY_ALL);
        nachoTextView.addChipTerminator(' ', ChipTerminatorHandler.BEHAVIOR_CHIPIFY_TO_TERMINATOR);
        nachoTextView.enableEditChipOnTouch(false, false);
        nachoTextView.setOnChipClickListener(new NachoTextView.OnChipClickListener() {
            @Override
            public void onChipClick(Chip chip, MotionEvent motionEvent) {
                System.out.println("Chip chip, MotionEvent motionEvent) {");
            }
        });
    }

    public String[] getAllUserEmails(ArrayList<User> users) {
        List<String> userEmailsList = new ArrayList<String>();
        if (users != null && users.size() > 0) {
            for (User user : users) {
                userEmailsList.add(user.getName());
            }
            return userEmailsList.toArray(new String[0]);
        }
        return new String['0'];

    }

    public boolean infosVerified() {
        boolean title_verified = validateInput.validateTitle();
        boolean address_verified = validateInput.validateAddress();
        boolean type_verified = validateInput.validateType();
        boolean nachoVerified = validateInput.validateNachoTextView();
        return title_verified && address_verified && type_verified && nachoVerified;
    }

    public void getEditTextsContent() {
        titleStr = titleEditText.getText().toString().trim();
        addressStr = addressEditText.getText().toString().trim();
        typeStr = typeEditText.getText().toString().trim();
        selectedEmailsList = nachoTextView.getChipValues();
    }

}