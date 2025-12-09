package com.vanvatcorporation.doubleclips.popups;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.vanvatcorporation.doubleclips.AdsHandler;
import com.vanvatcorporation.doubleclips.R;

public class AdsConsentPopup extends AlertDialog.Builder {
    public AdsConsentPopup(Context context, Activity activity) {
        super(context);


        // Inflate your custom layout
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.popup_asking_for_ads, null);
        this.setView(dialogView);

        // Get references to the EditText and Buttons in your custom layout
        Button rewardAdsButton = dialogView.findViewById(R.id.rewardedAdsButton);
        Button interstitialAdsButton = dialogView.findViewById(R.id.interstitialAdsButton);
        Button declineAdsButton = dialogView.findViewById(R.id.declineAdsButton);

        // Create the AlertDialog
        AlertDialog dialog = this.create();

        // Set button click listeners
        rewardAdsButton.setOnClickListener(vok -> {
            dialog.dismiss();
            AdsHandler.showRewardedAds(activity, AdsHandler.mRewardedAd);
        });
        // Set button click listeners
        interstitialAdsButton.setOnClickListener(vok -> {
            dialog.dismiss();
            AdsHandler.showInterstitialAds(activity, AdsHandler.mInterstitialAd);
        });

        declineAdsButton.setOnClickListener(vcan -> {
            // Just dismiss the dialog
            dialog.dismiss();
        });

        // Show the dialog
        dialog.show();
    }



    public static class AdsThanksLetterPopup extends AlertDialog.Builder {
        public AdsThanksLetterPopup(Context context) {
            super(context);

            // Inflate your custom layout
            LayoutInflater inflater = LayoutInflater.from(context);
            View dialogView = inflater.inflate(R.layout.popup_thanks_for_showing_ads, null);
            setView(dialogView);


            setNegativeButton(context.getText(R.string.close), (dialog, which) -> {
                dialog.dismiss();
            });

            // Create the AlertDialog
            AlertDialog dialog = this.create();

            // Show the dialog
            dialog.show();
        }
    }
}
