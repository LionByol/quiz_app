package com.silver.sponsor;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.NumberPicker;
import android.widget.RadioButton;

import com.silver.sponsor.reference.GR;
import com.silver.sponsor.reference.SendRequest;

public class RegisterActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        maleChk = (RadioButton)findViewById(R.id.maleChk);
        femaleChk = (RadioButton)findViewById(R.id.femaleChk);

        year = (NumberPicker)findViewById(R.id.yearpicker);
        year.setMaxValue(2099);
        year.setMinValue(1930);
        year.setValue(!GR.useryear.equals("_X_") ? Integer.parseInt(GR.useryear) : 2000);

        mon = (NumberPicker)findViewById(R.id.monthpicker);
        mon.setMaxValue(12);
        mon.setMinValue(1);
        mon.setValue(!GR.usermon.equals("_X_") ? Integer.parseInt(GR.usermon) : 6);
        mon.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                date.setMaxValue(maxmon[newVal]);
            }
        });

        date = (NumberPicker)findViewById(R.id.datepicker);
        date.setMaxValue(31);
        date.setMinValue(1);
        date.setValue(!GR.userdate.equals("_X_") ? Integer.parseInt(GR.userdate) : 15);

        Button continuebtn = (Button)findViewById(R.id.continuebtn);
        continuebtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String gender = maleChk.isChecked()?"male":"female";
                String birth = year.getValue()+"-"+mon.getValue()+"-"+date.getValue();

                new SendRequest().execute(GR.host+"register.php?id="+GR.userid+"&gender="+gender+"&birth="+birth);

                finish();
                Intent intent = new Intent(RegisterActivity.this, GameMenu.class);
                startActivity(intent);
            }
        });
    }


    public void onBackPressed()
    {
        super.onBackPressed();
        finish();
        startActivity(new Intent(this, LoginActivity.class));
    }

    RadioButton maleChk, femaleChk;
    NumberPicker year, mon, date;
    int maxmon[] = {0, 31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
}
