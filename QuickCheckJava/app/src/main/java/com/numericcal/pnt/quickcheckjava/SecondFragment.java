package com.numericcal.pnt.quickcheckjava;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    private LiveCameraFeed viewFeed;
    private String QUICKCHECK_CLIENT_ID = "your client id";
    private String QUICKCHECK_CLIENT_SECRET = "your service key";
    private View view;
    private static Logger LOGGER = Logger.getLogger("QuicCheckJava-Demo");
    private static int PERMISSION_REQUEST_CODE = 6699; // if 6 was 9 ... I don't mind ...

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_second, container, false);

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
        viewFeed = view.findViewById(R.id.quickcheck_live_feed);
        CompletableFuture<QuickCheck> qcFuture = QuickCheck.Static.getInstance(
                this,
                viewFeed,
                QUICKCHECK_CLIENT_ID,
                QUICKCHECK_CLIENT_SECRET
        );

        qcFuture.whenComplete(new BiConsumer<QuickCheck, Throwable>() {
            @Override
            public void accept(QuickCheck quickcheck, Throwable err) {
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

                    // wire up QuickCheck with the system
                }
            }
        });
        // DONE!
        ////////////////////////////////////////////////////////////////////////////////////////////
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // standard boilerplate

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
