package com.ebinbenny.ranking;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;
import com.google.firebase.dynamiclinks.PendingDynamicLinkData;

public class LaunchLinkActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        final LaunchLinkActivity launchLinkActivity = this;
        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(getIntent())
                .addOnSuccessListener(this, new OnSuccessListener<PendingDynamicLinkData>()
                {
                    @Override
                    public void onSuccess(PendingDynamicLinkData pendingDynamicLinkData)
                    {
                        if (pendingDynamicLinkData != null)
                        {
                            Uri deepLink = pendingDynamicLinkData.getLink();
                            String leagueId = deepLink.getQueryParameter("leagueID");
                            Intent intent = new Intent(launchLinkActivity, BeginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
                            intent.putExtra("leagueID",leagueId);
                            startActivity(intent);
                            launchLinkActivity.finish();
                        }
                    }
                })
                .addOnFailureListener(this, new OnFailureListener()
                {
                    @Override
                    public void onFailure(Exception e)
                    {
                        Intent intent = new Intent(launchLinkActivity, BeginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
                        startActivity(intent);
                        launchLinkActivity.finish();
                    }
                });
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
    }
}
