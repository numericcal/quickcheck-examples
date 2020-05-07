package com.numericcal.pnt.quickcheckjava;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.numericcal.pnt.qc.LiveCameraFeed;
import com.numericcal.pnt.qc.Messages;
import com.numericcal.pnt.qc.QuickCheck;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.logging.Logger;

public class SecondFragment extends Fragment {

    private QuickCheck qc;
    private String QUICKCHECK_CLIENT_ID = "your client id";
    private String QUICKCHECK_CLIENT_SECRET = "your service key";

    private View view;
    private Button snapButton;
    private Button resumeButton;
    private static Logger LOGGER = Logger.getLogger("QuickCheckJava-Demo");
    private static int PERMISSION_REQUEST_CODE = 6699; // if 6 was 9 ... I don't mind ...

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_second, container, false);
        snapButton = view.findViewById(R.id.snap_button);
        resumeButton = view.findViewById(R.id.resume_button);

        disable(snapButton);
        disable(resumeButton);

        if (allPermissionsGranted()) {
            setupQuickCheckService();
        } else {
            this.requestPermissions(QuickCheck.Static.getREQUIRED_PERMISSIONS(),
                    PERMISSION_REQUEST_CODE);
        }

        return view;
    }

    private void setupQuickCheckService() {
        ////////////////////////////////////////////////////////////////////////////////////////////
        // set up QuickCheck service
        LiveCameraFeed viewFeed = view.findViewById(R.id.quickcheck_live_feed);
        CompletableFuture<QuickCheck> qcFuture = QuickCheck.Static.getInstance(
                this,
                viewFeed,
                QUICKCHECK_CLIENT_ID,
                QUICKCHECK_CLIENT_SECRET
        );

        qcFuture.whenComplete((quickcheck, err) -> {
            QuickCheck.Static.QuickCheckException exc =
                    (QuickCheck.Static.QuickCheckException) err;
            if (err != null) {
                // error during the QuickCheck initialization
                LOGGER.info("Could not initialize QuickCheck: [" +
                        exc.status.toString() + "] " + exc.msg);
            } else {
                // QuickCheck service is ready
                qc = quickcheck;
                Toast.makeText(getContext(), "QuickCheck ready!",
                        Toast.LENGTH_LONG).show();

                ////////////////////////////////////////////////////////////////////////////////
                // wire up QuickCheck with the rest of the logic
                snapButton.setOnClickListener(v -> {
                    disable(snapButton);
                    CompletableFuture<Messages.SnapResult> result = qc.snap();
                    result.whenComplete((read, error) -> {
                        if (error != null) {
                            LOGGER.info("Something went wrong. " + error.getMessage());
                        } else {
                            switch (read.status) {
                                case SUCCESS: {
                                    // yay!
                                    LOGGER.info("Great, the read is ready!");
                                    resumeButton.setOnClickListener(v1 -> {
                                        qc.resume();
                                        disable(resumeButton);
                                        enable(snapButton);
                                    });
                                    enable(resumeButton);
                                    break;
                                }
                                case NOT_READY: {
                                    // sending too many too soon; debounce?
                                    LOGGER.info("QuickCheck is not ready for the next snap()!");
                                    qc.resume();
                                    enable(snapButton);
                                    break;
                                }
                                default: {
                                    LOGGER.info("Check the logs. Anything worth reporting on GitHub?");
                                    qc.resume();
                                    enable(snapButton);
                                    break;
                                }
                            }
                        }
                    });
                });

                enable(snapButton);
            }
            // logic ends
            ////////////////////////////////////////////////////////////////////////////////////////
        });
        // DONE!
        ////////////////////////////////////////////////////////////////////////////////////////////
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // the standard boilerplate

    private void disable(Button button) {
        button.setEnabled(false);
    }
    private void enable(Button button) {
        button.setEnabled(true);
    }

    private boolean allPermissionsGranted() {
        String[] requests = QuickCheck.Static.getREQUIRED_PERMISSIONS();
        boolean allTrue = true;
        for(String request : requests) {
            allTrue = allTrue &&
                    ContextCompat.checkSelfPermission(getContext(), request) ==
                            PackageManager.PERMISSION_GRANTED;
        }
        return allTrue;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (allPermissionsGranted()) {
                setupQuickCheckService();
            } else {
                Toast.makeText(getContext(),
                        "Permissions not granted by the user.", Toast.LENGTH_LONG).show();
            }
        }
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.button_second).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(SecondFragment.this)
                        .navigate(R.id.action_SecondFragment_to_FirstFragment);
            }
        });
    }
}
