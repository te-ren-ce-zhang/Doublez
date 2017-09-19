package com.example.doublez;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


public class Login extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        // EditText
        final EditText username=(EditText)findViewById(R.id.username_blank);
        final EditText password=(EditText)findViewById(R.id.password_blank);
        password.setTransformationMethod(PasswordTransformationMethod.getInstance());

        // Button
        Button login=(Button)findViewById(R.id.login);
        login.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                String inputUsername=username.getText().toString();
                String inputPassword=password.getText().toString();
                if(inputUsername.equals("admin") && inputPassword.equals("admin"))
                {
                    Intent intent=new Intent(Login.this,MainActivity.class);
                    intent.putExtra("Username",inputUsername);
                    startActivity(intent);
                    finish();
                }
                else
                {
                    AlertDialog.Builder dialog=new AlertDialog.Builder(Login.this);
                    dialog.setTitle("Username or Password not correct!");
                    dialog.setMessage("Please try again.");
                    dialog.setCancelable(true);
                    dialog.setPositiveButton("Okay",new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            EditText editText=(EditText)findViewById(R.id.password_blank);
                            editText.setText("");
                        }
                    });
                    dialog.show();
                }
            }
        });
    }

}