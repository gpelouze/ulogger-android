/*
 * Copyright (c) 2017 Bartek Fabiszewski
 * http://www.fabiszewski.net
 *
 * This file is part of μlogger-android.
 * Licensed under GPL, either version 3, or any later.
 * See <http://www.gnu.org/licenses/>
 */

package net.fabiszewski.ulogger;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Background service logging positions to database
 * and synchronizing with remote server.
 *
 */

public class LogAutomatorService extends Service {

    public static final String BROADCAST_AUTOMATOR_STARTED = "net.fabiszewski.ulogger.broadcast.log_automator_started";
    public static final String BROADCAST_AUTOMATOR_STOPPED = "net.fabiszewski.ulogger.broadcast.log_automator_stopped";
    public static final String BROADCAST_TRACK_CHANGED = "net.fabiszewski.ulogger.broadcast.track_changed";

    private static volatile boolean isRunning = false;
    private DbAccess db;

    /**
     * Basic initializations.
     */
    @Override
    public void onCreate() {
    }

    /**
     * Start main thread, request location updates, start synchronization.
     *
     * @param intent Intent
     * @param flags Flags
     * @param startId Unique id
     * @return Always returns START_STICKY
     */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        setRunning(true);
        sendBroadcast(BROADCAST_AUTOMATOR_STARTED);
        startLogger();
        changeTrack();
        return START_STICKY;
    }

    /**
     * Service cleanup
     */
    @Override
    public void onDestroy() {
        stopLogger();
        setRunning(false);
        sendBroadcast(BROADCAST_AUTOMATOR_STOPPED);
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Start logger service
     */
    private void startLogger() {
        Intent intent = new Intent(LogAutomatorService.this, LoggerService.class);
        startService(intent);
    }

    /**
     * Stop logger service
     */
    private void stopLogger() {
        Intent intent = new Intent(LogAutomatorService.this, LoggerService.class);
        stopService(intent);
    }

    /**
     * Change current track
     */
    private void changeTrack() {
        boolean restart_logger = false;
        if (LoggerService.isRunning()) {
            restart_logger = true;
            stopLogger();
        }

        DbAccess db = DbAccess.getInstance();
        db.open(this);
        db.newTrack(AutoNamePreference.getAutoTrackName(this));
        db.close();

        sendBroadcast(BROADCAST_TRACK_CHANGED);

        if (restart_logger) {
            startLogger();
        }
    }



    /**
     * Send broadcast message
     * @param broadcast Broadcast message
     */
    private void sendBroadcast(String broadcast) {
        Intent intent = new Intent(broadcast);
        sendBroadcast(intent);
    }

    /**
     * Check if logger service is running.
     *
     * @return True if running, false otherwise
     */
    public static boolean isRunning() {
        return isRunning;
    }

    /**
     * Set service running state
     * @param isRunning True if running, false otherwise
     */
    private void setRunning(boolean isRunning) {
        LogAutomatorService.isRunning = isRunning;
    }

}
