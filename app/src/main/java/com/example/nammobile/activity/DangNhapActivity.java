package com.example.nammobile.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nammobile.R;
import com.example.nammobile.retrofit.ApiBanHang;
import com.example.nammobile.retrofit.RetrofitClient;
import com.example.nammobile.utils.Utils;

import org.w3c.dom.Text;

import io.paperdb.Paper;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class DangNhapActivity extends AppCompatActivity {
    TextView txtdangky, txtresetmatkhau;
    EditText email, password;
    AppCompatButton btndangnhap;
    ApiBanHang apiBanHang;
    CompositeDisposable compositeDisposable = new CompositeDisposable();
    boolean isDangNhap = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dang_nhap);
        initView();
        initControll();
    }

    private void initControll() {
        txtdangky.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), DangKyActivity.class);
                startActivity(intent);
            }
        });
        txtresetmatkhau.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ResetMatKhauActivity.class);
                startActivity(intent);
            }
        });


        btndangnhap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String str_email = email.getText().toString().trim();
                String str_password = password.getText().toString().trim();
                if (TextUtils.isEmpty(str_email)){
                    Toast.makeText(getApplicationContext(),"Bạn chưa nhập Email", Toast.LENGTH_SHORT).show();
                }else if (TextUtils.isEmpty(str_password)){
                    Toast.makeText(getApplicationContext(),"Bạn chưa nhập mật khẩu", Toast.LENGTH_SHORT).show();
                }else {
                    //Lưu thông tin tài khoản
                    Paper.book().write("email", str_email);
                    Paper.book().write("password", str_password);
                    dangNhap(str_email,str_password);
                }
            }
        });
    }

    private void initView() {
        Paper.init(this);
        apiBanHang = RetrofitClient.getInstance(Utils.BASE_URL).create(ApiBanHang.class);
        txtdangky = findViewById(R.id.txtdangky);
        txtresetmatkhau = findViewById(R.id.txtresetmatkhau);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        btndangnhap = findViewById(R.id.btndangnhap);

        //Đọc Data
        if (Paper.book().read("email") != null && Paper.book().read("password") != null){
            email.setText(Paper.book().read("email"));
            password.setText(Paper.book().read("password"));
            if (Paper.book().read("isDangNhap") != null){
                boolean flag = Paper.book().read("isDangNhap");
                if (flag){
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //dangNhap(Paper.book().read("email"), Paper.book().read("password"));
                        }
                    },1000);
                }
            }
        }
        
    }

    private void dangNhap(String email, String password) {
        compositeDisposable.add(apiBanHang.dangnhap(email,password)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        userModel -> {
                            if (userModel.isSuccess()){
                                isDangNhap = true;
                                Paper.book().write("isDangNhap", isDangNhap);
                                Utils.user_current = userModel.getResult().get(0);
                                // Lưu lại thông tin của người dùng
                                Paper.book().write("user", userModel.getResult().get(0));
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        },
                        throwable -> {
                            Toast.makeText(getApplicationContext(),throwable.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                ));
            }

    @Override
    protected void onResume() {
        super.onResume();
        if (Utils.user_current.getEmail() != null && Utils.user_current.getPassword() != null){
            email.setText(Utils.user_current.getEmail());
            password.setText(Utils.user_current.getPassword());
        }
    }

    @Override
    protected void onDestroy() {
        compositeDisposable.clear();
        super.onDestroy();
    }
}