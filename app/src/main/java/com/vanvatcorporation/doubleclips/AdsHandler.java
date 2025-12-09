package com.vanvatcorporation.doubleclips;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.vanvatcorporation.doubleclips.popups.AdsConsentPopup;

public class AdsHandler {

    public static RewardedAd mRewardedAd;
    public static InterstitialAd mInterstitialAd;
    public static boolean wasPreviouslyShowingAds;

    public static void loadRewardedAds(Context context, Activity activity)
    {
        RewardedAd.load(context, "ca-app-pub-1708105276845874/6458040635", // Test Ad Unit ca-app-pub-3940256099942544/5224354917
                new AdRequest.Builder().build(), new RewardedAdLoadCallback() {
            @Override
            public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                // Ad successfully loaded
                mRewardedAd = rewardedAd;
                if(mInterstitialAd != null)
                {
                    new AdsConsentPopup(context, activity);
                }
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                // Handle the error
                mRewardedAd = null;
            }
        });
    }

    public static void showRewardedAds(Activity activity, RewardedAd mRewardedAd)
    {
        if (mRewardedAd != null) {
            mRewardedAd.show(activity, rewardItem -> {
                // Handle the reward
                int rewardAmount = rewardItem.getAmount();
                String rewardType = rewardItem.getType();
                wasPreviouslyShowingAds = true;
            });
        }
    }


    public static void loadInterstitialAd(Context context, Activity activity) {
        AdRequest adRequest = new AdRequest.Builder().build();

        InterstitialAd.load(context,
                "ca-app-pub-1708105276845874/3681324746", // Test Ad Unit ID ca-app-pub-3940256099942544/1033173712
                adRequest,
                new InterstitialAdLoadCallback() {
                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        mInterstitialAd = interstitialAd;
//                        Log.i(TAG, "Ad was loaded.");
                        if(mRewardedAd != null)
                        {
                            new AdsConsentPopup(context, activity);
                        }

                        // Set callbacks
                        mInterstitialAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdDismissedFullScreenContent() {
//                                Log.d(TAG, "Ad dismissed.");

                                mInterstitialAd = null;
                                loadInterstitialAd(context, activity); // Preload next ad
                                wasPreviouslyShowingAds = true;
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(AdError adError) {
//                                Log.e(TAG, "Ad failed to show: " + adError.getMessage());
                                mInterstitialAd = null;
                            }

                            @Override
                            public void onAdShowedFullScreenContent() {
//                                Log.d(TAG, "Ad showed fullscreen content.");
                                mInterstitialAd = null;
                            }
                        });
                    }

                    @Override
                    public void onAdFailedToLoad(LoadAdError loadAdError) {
//                        Log.e(TAG, "Ad failed to load: " + loadAdError.getMessage());
                        mInterstitialAd = null;
                    }
                });
    }

    public static void showInterstitialAds(Activity activity, InterstitialAd mInterstitialAd)
    {
        if (mInterstitialAd != null) {
            mInterstitialAd.show(activity);
        }
    }

    public static void loadBothAds(Context context, Activity activity) {
        AdsHandler.loadRewardedAds(context, activity);
        AdsHandler.loadInterstitialAd(context, activity);
    }
    public static void initializeAds(Context context, Activity activity) {
        MobileAds.initialize(context, initializationStatus -> {
            loadBothAds(context, activity);
        });
    }


    public static void displayThanksForShowingAds(Context context)
    {
        if(wasPreviouslyShowingAds)
        {
            wasPreviouslyShowingAds = false;
            new AdsConsentPopup.AdsThanksLetterPopup(context);
        }
    }
}
