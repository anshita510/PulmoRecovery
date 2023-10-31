package com.example.tribecovidmonitor;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class OnBoardingDesignOne extends AppCompatActivity {
    private com.example.tribecovidmonitor.OnboardingAdapter onboardingAdapter;
    private LinearLayout layoutOnboardingIndicator;
    private MaterialButton buttonOnboardingAction;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.boardingdesignone);
        layoutOnboardingIndicator = findViewById(R.id.layoutOnboardingIndicators);
        buttonOnboardingAction = findViewById(R.id.buttonOnBoardingAction);
        setOnboardingItem();
        ViewPager2 onboardingViewPager = findViewById(R.id.onboardingViewPager);
        onboardingViewPager.setAdapter(onboardingAdapter);
        setOnboadingIndicator();
        setCurrentOnboardingIndicators(0);
        onboardingViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                setCurrentOnboardingIndicators(position);
            }
        });
        buttonOnboardingAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onboardingViewPager.getCurrentItem() + 1 < onboardingAdapter.getItemCount()) {
                    onboardingViewPager.setCurrentItem(onboardingViewPager.getCurrentItem() + 1);
                } else {
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    finish();
                }
            }
        });
    }
    private void setOnboadingIndicator() {
        ImageView[] indicators = new ImageView[onboardingAdapter.getItemCount()];
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(8, 0, 8, 0);
        for (int i = 0; i < indicators.length; i++) {
            indicators[i] = new ImageView(getApplicationContext());
//            indicators[i].setImageDrawable(ContextCompat.getDrawable(
//                    getApplicationContext(), R.drawable.onboarding_indicator_inactive
//            ));
            indicators[i].setLayoutParams(layoutParams);
            layoutOnboardingIndicator.addView(indicators[i]);
        }
    }
    @SuppressLint("SetTextI18n")
    private void setCurrentOnboardingIndicators(int index) {
        int childCount = layoutOnboardingIndicator.getChildCount();
        for (int i = 0; i < childCount; i++) {
            ImageView imageView = (ImageView) layoutOnboardingIndicator.getChildAt(i);
            if (i == index) {
//                imageView.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.onboarding_indicator_active));
           } else {
//                imageView.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.onboarding_indicator_inactive));
            }
        }
        if (index == onboardingAdapter.getItemCount()- 1){
            buttonOnboardingAction.setText("Start");
        }else {
            buttonOnboardingAction.setText("Next");
        }
    }
    private void setOnboardingItem() {
        List<OnBoardingItem> onBoardingItems = new ArrayList<>();
        OnBoardingItem itemFastFood = new OnBoardingItem();
        itemFastFood.setTitle("Breathing sound-based Continuous COVID-19 Condition Monitoring System");
        itemFastFood.setDescription("Bringing COVID Monitoring solutions to your Home");
       itemFastFood.setImage(R.drawable.logo);
        OnBoardingItem itemPayOnline = new OnBoardingItem();
        itemPayOnline.setTitle("Contribute to the TribeConnect project");
        itemPayOnline.setDescription("Record your voice sample for research");
        itemPayOnline.setImage(R.drawable.tribeconnect);
        OnBoardingItem itemEatTogether = new OnBoardingItem();
        itemEatTogether.setTitle("Please follow the instructions.");
        itemEatTogether.setDescription("Step 1: Connect your microphone to your smartphone");
        itemEatTogether.setImage(R.drawable.design);
        OnBoardingItem itemnewTogether = new OnBoardingItem();
        itemnewTogether.setTitle("Please follow the instructions.");
        itemnewTogether.setDescription(" Step 2: Bring your microphone near to your nose");
        itemnewTogether.setImage(R.drawable.ins);
        OnBoardingItem itemnewTogether1 = new OnBoardingItem();
        itemnewTogether1.setTitle("Lets record now!");
        itemnewTogether1.setDescription("");
//        itemEatTogether.setImage(R.drawable.eat_together);
        onBoardingItems.add(itemFastFood);
        onBoardingItems.add(itemPayOnline);
        onBoardingItems.add(itemEatTogether);
        onBoardingItems.add(itemnewTogether);
        onBoardingItems.add(itemnewTogether1);
        onboardingAdapter = new com.example.tribecovidmonitor.OnboardingAdapter(onBoardingItems);
    }
}