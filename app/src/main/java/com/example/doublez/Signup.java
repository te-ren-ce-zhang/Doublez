package com.example.doublez;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;

public class Signup extends AppCompatActivity
{
    public static final int CHOOSE_PHOTO=2;

    private ImageView avatarImage;
    private Bitmap avatarBMP;
    private byte[] avatar;
    private EditText username;
    private EditText email;
    private EditText password;
    private EditText repassword;

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);


        Resources res = Signup.this.getResources();
        avatarBMP = BitmapFactory.decodeResource(res, R.mipmap.ic_launcher);

        //Toolbar
        Toolbar toolbar=(Toolbar)findViewById(R.id.toolbar_r);
        setSupportActionBar(toolbar);

        avatarImage=(ImageView)findViewById(R.id.signup_avatar);
        avatarImage.setImageBitmap(avatarBMP);

        //选择头像键
        Button chooseAvatar=(Button)findViewById(R.id.signup_choose);
        chooseAvatar.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(ContextCompat.checkSelfPermission(Signup.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED)
                {
                    ActivityCompat.requestPermissions(Signup.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                }
                else
                    openAlbum();
            }
        });

        username=(EditText)findViewById(R.id.signup_username);
        email=(EditText)findViewById(R.id.signup_email);
        password=(EditText)findViewById(R.id.signup_password);
        repassword=(EditText)findViewById(R.id.signup_repassword);
        // 密码星号化
        password.setTransformationMethod(PasswordTransformationMethod.getInstance());
        repassword.setTransformationMethod(PasswordTransformationMethod.getInstance());

        //Sign up键
        Button signup=(Button)findViewById(R.id.signup);
        signup.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if(username.getText().toString().equals("") || email.getText().toString().equals("") || password.getText().toString().equals("") || repassword.getText().toString().equals(""))
                {
                    // AlertDialog
                    AlertDialog.Builder dialog=new AlertDialog.Builder(Signup.this);
                    dialog.setTitle("有信息未填");
                    dialog.setMessage("请填写完整");
                    dialog.setCancelable(true);
                    dialog.setPositiveButton("好",new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            password.setText("");
                            repassword.setText("");
                        }
                    });
                    dialog.show();
                }
                else
                {
                    if(password.getText().toString().equals(repassword.getText().toString()))
                    {
                        // 计入数据库，释放Activit
                        User user=new User(username.getText().toString(),email.getText().toString(),password.getText().toString(),img(avatarBMP));
                        user.save();
                        Toast.makeText(Signup.this,"注册成功", Toast.LENGTH_SHORT).show();
                        Intent intent=new Intent(Signup.this,Login.class);
                        startActivity(intent);
                        finish();


                    }
                    else
                    {
                        // AlertDialog
                        AlertDialog.Builder dialog=new AlertDialog.Builder(Signup.this);
                        dialog.setTitle("密码不一致");
                        dialog.setMessage("请重新输入");
                        dialog.setCancelable(true);
                        dialog.setPositiveButton("好",new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                password.setText("");
                                repassword.setText("");
                            }
                        });
                        dialog.show();
                    }
                }
            }
        });
    }

    private void openAlbum()
    {
        Intent intent=new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent,CHOOSE_PHOTO);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions, int[]grantResults)
    {
        switch(requestCode)
        {
            case 1:
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED)
                    openAlbum();
                break;
            default:
        }
    }

    public void onActivityResult(int RequestCode,int ResultCode,Intent data){
        switch(RequestCode){
            case 2:if(ResultCode==RESULT_OK){
                if(Build.VERSION.SDK_INT>=19){
                    handleImageOnKitKat(data);
                }else{
                    handleImageBeforeKitKat(data);
                }
            }break;
            default:
        }
    }

    @TargetApi(19)
    private void handleImageOnKitKat(Intent data){
        String imagePath=null;
        Uri uri=data.getData();
        if(DocumentsContract.isDocumentUri(this,uri)){
            String docId=DocumentsContract.getDocumentId(uri);
            if("com.android.providers.media.documents".equals(uri.getAuthority())){
                String id=docId.split(":")[1];
                String selection= MediaStore.Images.Media._ID+"="+id;
                imagePath=getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,selection);
            }else if("com.android.providers.downloads.documents".equals(uri.getAuthority())){
                Uri contentUri= ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"),Long.valueOf(docId));
                imagePath=getImagePath(contentUri,null);
            }
        }else if("content".equalsIgnoreCase(uri.getScheme())){
            imagePath=getImagePath(uri,null);
        }else if("file".equalsIgnoreCase(uri.getScheme())){
            imagePath=uri.getPath();
        }
        displayImage(imagePath);
    }

    private void handleImageBeforeKitKat(Intent data){
        String imagePath=null;
        Uri uri=data.getData();
        imagePath=getImagePath(uri,null);
        displayImage(imagePath);
    }

    private String getImagePath(Uri uri,String selection){
        String Path=null;
        Cursor cursor=getContentResolver().query(uri,null,selection,null,null);
        if(cursor!=null){
            if(cursor.moveToFirst()){
                Path=cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return Path;
    }

    private void displayImage(String Path){
        Bitmap bm= BitmapFactory.decodeFile(Path);
        avatarImage.setImageBitmap(bm);
        avatarBMP=bm;
    }

    private byte[]img(Bitmap bitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        return baos.toByteArray();
    }
}
