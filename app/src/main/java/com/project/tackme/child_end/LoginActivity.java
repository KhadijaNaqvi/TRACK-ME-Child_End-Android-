package com.project.tackme.child_end;


import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class LoginActivity extends AppCompatActivity {

    private Button mbtnLogin;
    private TextInputEditText mtextEmail,mtextPass;
    private static String pass,email,referenceChild;
    private FirebaseAuth firebaseAuth;
    private static FirebaseFirestore firebaseFirestore;
    private static SharedPreferences sharedPreferences;
    private static SharedPreferences.Editor mEditor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mbtnLogin=findViewById(R.id.btn_login);
        sharedPreferences=getSharedPreferences("MySharedPref",MODE_PRIVATE);
        mEditor=sharedPreferences.edit();


        mtextEmail=findViewById(R.id.customer_email_login);
        mtextPass=findViewById(R.id.customer_pass_login);
        firebaseAuth=FirebaseAuth.getInstance();
        firebaseFirestore=FirebaseFirestore.getInstance();

        mbtnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                email=mtextEmail.getText().toString().trim();
                pass=mtextPass.getText().toString().trim();

                if(TextUtils.isEmpty(email) || TextUtils.isEmpty(pass)){
                    if (TextUtils.isEmpty(email)){ mtextEmail.setError("Please Enter Email");}
                    if(TextUtils.isEmpty(pass)){mtextPass.setError("Please Enter Password ");}
                }
                else{

                    getChildReference();
                    Log.e("onClick: ",""+referenceChild );
                   }

            }
        });


    }


    public  void getChildReference(){
        firebaseFirestore.collection("childdata").whereEqualTo("email",email).whereEqualTo("password",pass).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(QueryDocumentSnapshot snapshot : task.getResult()){
                        DocumentReference reference=snapshot.getReference();
                        referenceChild=reference.getId();
                        mEditor.putString("referenceChild",referenceChild);
                        mEditor.putString("email",email);
                        mEditor.putString("password",pass);
                        mEditor.putString("referenceParent",
                                snapshot.getString("id").toString().trim());
                        mEditor.putBoolean("login_state",true);
                        mEditor.commit();
                        Intent intent=new Intent(getApplicationContext(),MainActivity.class);
                        startActivity(intent);
                        finish();

                         Log.e( "Reference : ", "ID" +referenceChild);
                    }

                }else {


                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                e.printStackTrace();
            }
        });

    }


}


