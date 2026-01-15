/**
 * Wire
 * Copyright (C) 2018 Wire Swiss GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package anubhav.calculatorapp;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.CancellationSignal;

import androidx.core.app.ActivityCompat;

/**
 * Created by Cameron on 1/30/2018.
 */

public class FingerprintHandler extends
        FingerprintManager.AuthenticationCallback {

    private CancellationSignal cancellationSignal;
    private Context appContext;
    private Intent intent;
    public  Boolean isSignal;

    public FingerprintHandler(Context context, Boolean isSignal) {
        appContext = context;
        this.isSignal = isSignal;
    }

    public void startAuth(FingerprintManager manager,
                          FingerprintManager.CryptoObject cryptoObject) {

        cancellationSignal = new CancellationSignal();

        if (ActivityCompat.checkSelfPermission(appContext,
                Manifest.permission.USE_FINGERPRINT) !=
                PackageManager.PERMISSION_GRANTED) {
            return;
        }
        manager.authenticate(cryptoObject, cancellationSignal, 0, this, null);
    }

    @Override
    public void onAuthenticationError(int errMsgId,
                                      CharSequence errString) {
        //Toast.makeText(appContext,
        //   "Authentication error\n" + errString,
        //  Toast.LENGTH_LONG).show();
    }

    @Override
    public void onAuthenticationHelp(int helpMsgId,
                                     CharSequence helpString) {
        //Toast.makeText(appContext,
        //   "Authentication help\n" + helpString,
        //  Toast.LENGTH_LONG).show();
    }

    @Override
    public void onAuthenticationFailed() {
        //Toast.makeText(appContext,
        //   "Authentication failed.",
        //  Toast.LENGTH_LONG).show();
    }

    @Override
    public void onAuthenticationSucceeded(
            FingerprintManager.AuthenticationResult result) {

        //Toast.makeText(appContext,
        //   "Authentication succeeded.",
        //  Toast.LENGTH_LONG).show();

        if (isSignal) {
            intent = new Intent("android.intent.action.MAIN");
            intent.setComponent(new ComponentName("org.thoughtcrime.securesms", "org.thoughtcrime.securesms.RoutingActivity"));
        }
        else
        {
            intent = new Intent("android.intent.action.MAIN");
            //intent = new Intent(appContext, org.standardnotes.notes.MainActivity.class);
            intent.setComponent(new ComponentName("org.thoughtcrime.securesms", "org.standardnotes.notes.MainActivity"));
        }
        appContext.startActivity(intent);
    }
}
