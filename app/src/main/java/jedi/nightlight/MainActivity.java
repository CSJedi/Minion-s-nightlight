package jedi.nightlight;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements Animation.AnimationListener  {
    private ViewPager viewPager;
    private ViewPagerAdapter viewPagerAdapter;
    private int[] layouts;
    private ImageButton btnPrevious, btnNext;
    private boolean isPlay = true;
    private int lightPower = 1;
    private Intent soundService;
    private ImageView logo;
    private Animation animation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.System.canWrite(MainActivity.this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                intent.setData(Uri.parse("package:" + this.getPackageName()));
                startActivityForResult(intent, 1);
            }
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        componentsInit();
        setBrightness(127);
        animateBackground();
    }

    private void setBrightness(int brightnessPower){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.System.canWrite(MainActivity.this)) {
                Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, brightnessPower);
            }
        }else{
            Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, brightnessPower);
        }
    }

    private void animateBackground(){
        animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.rotate);
        logo = (ImageView) findViewById(R.id.imageView);

        animation.setAnimationListener(MainActivity.this);
        logo.setVisibility(View.VISIBLE);
        logo.startAnimation(animation);
    }

    private void componentsInit(){
        viewPager = (ViewPager) findViewById(R.id.view_pager);
        btnPrevious = (ImageButton) findViewById(R.id.btn_previous);
        btnNext = (ImageButton) findViewById(R.id.btn_next);

        (findViewById(R.id.btn_sound)).setOnClickListener(soundPlayButton);
        (findViewById(R.id.btn_light)).setOnClickListener(lightButton);

        layouts = new int[]{
                R.layout.minion0,
                R.layout.minion1,
                R.layout.minion2};

        soundService = new Intent(MainActivity.this, BackgroundSoundService.class);
        startService(soundService);

        viewPagerAdapter = new ViewPagerAdapter();
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.addOnPageChangeListener(viewPagerPageChangeListener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "Permission got", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Permission denied", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    @Override
    public void onAnimationStart(Animation animation) {
    }
    @Override
    public void onAnimationEnd(Animation animation) {
    }
    @Override
    public void onAnimationRepeat(Animation animation) {
    }

    View.OnClickListener lightButton = new View.OnClickListener(){

        public void onClick(View v){
            int curBrightnessValue = 0;
            try {
                curBrightnessValue = Settings.System.getInt(
                        getContentResolver(),
                        android.provider.Settings.System.SCREEN_BRIGHTNESS);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }

            switch (lightPower){
                case 0: ((ImageButton) v).setImageResource(R.drawable.light_on);
                    curBrightnessValue = 127;lightPower++;
                    break;
                case 1: ((ImageButton) v).setImageResource(R.drawable.light_on_fullpower);
                    curBrightnessValue = 255; lightPower++;
                    break;
                case 2: ((ImageButton) v).setImageResource(R.drawable.light_off);
                    curBrightnessValue = 20; lightPower=0;
                    break;
            }

            setBrightness(curBrightnessValue);
        }
    };

    View.OnClickListener soundPlayButton = new View.OnClickListener(){

        public void onClick(View v){

            if(isPlay){
                onPauseSound();
                ((ImageButton) v).setImageResource(R.drawable.sound_off);
            }else{
                onResumeSound();
                ((ImageButton) v).setImageResource(R.drawable.sound_on);
            }
            isPlay = !isPlay;
        }
    };

    void onPauseSound(){
        stopService(soundService);
    }

    void onResumeSound(){
        startService(soundService);
    }

    public void btnPreviousClick(View v)
    {
        int current = getPreviousItem();
        if (current > -1) {
            viewPager.setCurrentItem(current);
        }
    }

    public void btnNextClick(View v)
    {
        int current = getNextItem();
        if (current < layouts.length) {
            viewPager.setCurrentItem(current);
        }
    }

    ViewPager.OnPageChangeListener viewPagerPageChangeListener = new ViewPager.OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {

        }

        @Override
        public void onPageScrollStateChanged(int arg0) {

        }
    };

    private int getNextItem() {
        return viewPager.getCurrentItem() + 1;
    }
    private int getPreviousItem() {
        return viewPager.getCurrentItem() - 1;
    }

    public class ViewPagerAdapter extends PagerAdapter {
        private LayoutInflater layoutInflater;
        public ViewPagerAdapter() {
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = layoutInflater.inflate(layouts[position], container, false);
            container.addView(view);

            return view;
        }

        @Override
        public int getCount() {
            return layouts.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object obj) {
            return view == obj;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            View view = (View) object;
            container.removeView(view);
        }

    }
}
