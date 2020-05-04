package com.numericcal.pnt.quickcheckjava;

import android.icu.util.UniversalTimeScale;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.arch.core.util.Function;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.numericcal.pnt.qc.LiveCameraFeed;
import com.numericcal.pnt.qc.Messages;
import com.numericcal.pnt.qc.QuickCheck;

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

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_second, container, false);

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
                QuickCheck.Static.QuickCheckException exc = (QuickCheck.Static.QuickCheckException) err;
                if (err != null) {
                    // error during the QuickCheck initialization
                    LOGGER.info("Could not initialize QuickCheck: [" + exc.status.toString() + "] " + exc.msg);
                } else {
                    // QuickCheck service is ready
                    qc = quickcheck;
                    Toast.makeText(getContext(), "QuickCheck ready!", Toast.LENGTH_SHORT).show();

                    // wire up QuickCheck with the system
                }
            }
        });

        return view;
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
