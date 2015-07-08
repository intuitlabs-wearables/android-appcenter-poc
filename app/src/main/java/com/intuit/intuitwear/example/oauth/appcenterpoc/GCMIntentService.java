/*
 * Copyright (c) 2015 Intuit Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.intuit.intuitwear.example.oauth.appcenterpoc;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;
import com.intuit.intuitwear.exceptions.IntuitWearException;
import com.intuit.intuitwear.model.IWAuthConstants;
import com.intuit.intuitwear.notifications.IWearAndroidNotificationSender;
import com.intuit.intuitwear.notifications.IWearNotificationSender;
import com.intuit.intuitwear.notifications.IWearNotificationType;
import com.intuit.mobile.png.sdk.PushNotificationsV2;
import com.intuit.mobile.png.sdk.UserTypeEnum;
import com.intuit.mobile.png.sdk.callback.RegisterUserCallback;


/**
 * GCMIntentService extends Google's {@link com.google.android.gcm.GCMBaseIntentService}, setting up the communication with
 * the Push Notification Gateway. Here we are adding the registration with Intuit's Notification
 * Server and also implement a few callbacks.
 */
public class GCMIntentService extends GCMBaseIntentService {
    private static final String LOG_TAG = GCMIntentService.class.getSimpleName();
//    private static final String REG_URL = "https://png.d2d.msg.intuit.com";

//    public static final String SENDER_ID_PROPERTY = "itduzzit.intuit.com.senderIdProperty";
//    public static final String GCM_PROJECT_NUMBER_PROPERTY = "itduzzit.intuit.com.gcmProjectProperty";
//    public static final String USERNAME_PROPERTY = "itduzzit.intuit.com.usernameProperty";
//    public static final String GROUPS_PROPERTY = "itduzzit.intuit.com.groupsProperty";

    private static String userid;
    private static String[] groups;

    /**
     * Default Constructor, requires Google GCM PROJECT_NUMBER,
     * which must be your GCM Project number and statically available.
     */
    public GCMIntentService() {
        super(MainOAuthActivityFragment.GCM_PROJECT_NUMBER);
    }

    /**
     * Register with GCM, which will eventually trigger {@link #onRegistered} to be called.
     *
     * @param context {@link android.content.Context} Application context
     * @param userid  {@link String} how your app refers to this user
     * @param groups  {@link String[]} may be null
     */
    protected static void register(final Context context, String userid, final String[] groups) {
        GCMIntentService.userid = userid;
        GCMIntentService.groups = groups;

        PushNotificationsV2.URL_OVERRIDE = IWAuthConstants.REG_URL;
        PushNotificationsV2.Environment environment = PushNotificationsV2.Environment.SANDBOX;
        PushNotificationsV2.initialize(MainOAuthActivityFragment.INTUIT_SENDER_ID, MainOAuthActivityFragment.GCM_PROJECT_NUMBER, environment);
        PushNotificationsV2.setLogging(true);
        PushNotificationsV2.registerForGCMNotifications(context);
    }

    /**
     * Google will call this method, providing you a unique registrationId for this device.
     * We recommended to save the registrationId to local preferences for later use.
     * e.g. saveRegistrationId(registrationId);
     *
     * @param context        {@link android.content.Context} Application context
     * @param registrationId {@link String} unique registrationId for this device
     */
    @Override
    protected void onRegistered(final Context context, final String registrationId) {

        PushNotificationsV2.registerUser(
                this,
                GCMIntentService.userid,
                UserTypeEnum.OTHER,
                GCMIntentService.groups,
                registrationId,
                new RegisterUserCallback() {


                    @Override
                    public void onUserRegistered() {
                        Log.i(LOG_TAG, "Registration call to PNG servers was accepted");
                    }

                    @Override
                    public void onError(String code, String description) {
                        Log.i(LOG_TAG, String.format("Received error callback from PNG. Error code= %s, description= %s", code, description));
                    }
                });
    }

    /**
     * This callback method is invoked when GCM delivers a notification to the device.
     * <p/>
     * Assuming that the json encoded dialogMessage is a valid (see IntuitWear JSONSchema) document,
     * we acquire an instance of a {@link com.intuit.intuitwear.notifications.IWearNotificationSender.Factory} to create a NotificationSender,
     * which will send the generated notification to the wearable device.
     *
     * @param context {@link android.content.Context} Application context
     * @param intent  {@link android.content.Intent} received with the push notification
     */
    @Override
    protected void onMessage(final Context context, final Intent intent) {
        Log.v(LOG_TAG, "Received onMessage call. Will now display a notification");
        final String message = intent.getStringExtra("payload").replaceAll("[\r\n]+$", "");
        Log.v(LOG_TAG, "===> Notification Payload = " + message);
        final IWearNotificationSender.Factory iWearSender = IWearNotificationSender.Factory.getsInstance();
        try {
            IWearAndroidNotificationSender androidNotificationSender =
                    (IWearAndroidNotificationSender) iWearSender.createNotificationSender(IWearNotificationType.ANDROID, this, message);
            androidNotificationSender.sendNotification(this);
        } catch (IntuitWearException e) {
            Log.e(LOG_TAG, e.toString());
        }
    }

    /**
     * Callback called upon a GCM error.
     *
     * @param context {@link android.content.Context} Application context
     * @param msg     {@link String} Error string
     */
    @Override
    protected void onError(final Context context, final String msg) {
        Log.e(LOG_TAG, "Error related to GCM: " + msg);
    }

    /**
     * Callback called when device is unregistered from GCM.
     *
     * @param context {@link android.content.Context} Application context
     * @param msg     Unregister dialogMessage
     */
    @Override
    protected void onUnregistered(final Context context, final String msg) {
        Log.i(LOG_TAG, "Received unregistered call");
    }
}

