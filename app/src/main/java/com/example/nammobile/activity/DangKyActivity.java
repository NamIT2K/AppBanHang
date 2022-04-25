package com.example.nammobile.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.nammobile.R;
import com.example.nammobile.retrofit.ApiBanHang;
import com.example.nammobile.retrofit.RetrofitClient;
import com.example.nammobile.utils.Utils;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class DangKyActivity extends AppCompatActivity {
    EditText email,username, password, repassword, sodienthoai;
    AppCompatButton button;
    ApiBanHang apiBanHang;
    CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dang_ky);
        initView();
        initControll();
    }

    private void initControll() {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dangky();
            }
        });
    }

    private void dangky() {
        String str_email = email.getText().toString().trim();
        String str_password = password.getText().toString().trim();
        String str_repassword = repassword.getText().toString().trim();
        String str_sodienthoai = sodienthoai.getText().toString().trim();
        String str_username = username.getText().toString().trim();
        if (TextUtils.isEmpty(str_email)){
            Toast.makeText(getApplicationContext(),"Bạn chưa nhập Email", Toast.LENGTH_SHORT).show();
        }else if (TextUtils.isEmpty(str_password)){
            Toast.makeText(getApplicationContext(),"Bạn chưa nhập mật khẩu", Toast.LENGTH_SHORT).show();
        }else if (TextUtils.isEmpty(str_repassword)){
            Toast.makeText(getApplicationContext(),"Bạn chưa nhập lại mật khẩu", Toast.LENGTH_SHORT).show();
        }else if (TextUtils.isEmpty(str_sodienthoai)){
            Toast.makeText(getApplicationContext(),"Bạn chưa nhập số điện thoại", Toast.LENGTH_SHORT).show();
        }else if (TextUtils.isEmpty(str_username)){
            Toast.makeText(getApplicationContext(),"Bạn chưa nhập tên đăng nhập", Toast.LENGTH_SHORT).show();
        }else {
            if (str_password.equals(str_repassword)){
                // post data
                compositeDisposable.add(apiBanHang.dangky(str_email,str_password,str_username,str_sodienthoai)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        userModel -> {
                            if (userModel.isSuccess()){
                                Utils.user_current.setEmail(str_email);
                                Utils.user_current.setPassword(str_password);
                                Intent intent = new Intent(getApplicationContext(), DangNhapActivity.class);
                                startActivity(intent);
                                finish();
                            }else {
                                Toast.makeText(getApplicationContext(),userModel.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        },
                        throwable -> {
                            Toast.makeText(getApplicationContext(),throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                ));
            }else {
                Toast.makeText(getApplicationContext(),"Password chưa khớp", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void initView() {
        apiBanHang = RetrofitClient.getInstance(Utils.BASE_URL).create(ApiBanHang.class);

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        repassword = findViewById(R.id.repassword);
        sodienthoai = findViewById(R.id.sodienthoai);
        username = findViewById(R.id.username);
        button = findViewById(R.id.btndangky);
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }
}