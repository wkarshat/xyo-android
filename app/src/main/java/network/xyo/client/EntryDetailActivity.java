package network.xyo.client;

import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import network.xyo.sdk.data.Entry;
import network.xyo.sdk.nodes.Node;

public class EntryDetailActivity extends AppCompatActivity {

    public static final String ENTRY_HASH = "entry_hash";

    private Entry entry;
    private Node node;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.entry_detail_activity);

        node = Node.get(getIntent().getStringExtra(NodeDetailFragment.NODE_NAME));
        entry = node.getEntry(getIntent().getStringExtra(EntryDetailActivity.ENTRY_HASH));

        setType(entry.getTypeString());
        setEpoch(entry.getEpochString());
        setNonce(entry.getNonceString());
        setDifficulty(entry.getDifficultyString());
        setPayloads(entry.getPayloadsString());
        setP1Keys(entry.getP1KeysString());
        setP2Keys(entry.getP2KeysString());
        setP1Signatures(entry.getP1SignaturesString());
        setP2Signatures(entry.getP2SignaturesString());
        setHeadKeys(entry.getHeadKeysString());
        setTailKeys(entry.getTailKeysString());
        setHeadSignatures(entry.getHeadSignaturesString());
        setTailSignatures(entry.getTailSignaturesString());

        refreshScrollViewSize();

    }

    private void refreshScrollViewSize() {
        NestedScrollView scrollView = ((NestedScrollView)findViewById(R.id.scroll));
        scrollView.invalidate();
        scrollView.requestLayout();
        LinearLayout container = ((LinearLayout)findViewById(R.id.container));
        container.invalidate();
        container.requestLayout();
    }

    private void setType(final String value) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView)findViewById(R.id.type)).setText("Type: " + value);
            }
        });
    }

    private void setEpoch(final String value) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView)findViewById(R.id.epoch)).setText("Epoch: " + value);
            }
        });
    }

    private void setNonce(final String value) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView)findViewById(R.id.nonce)).setText("Nonce: " + value);
            }
        });
    }

    private void setDifficulty(final String value) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView)findViewById(R.id.difficulty)).setText("Difficulty: " + value);
            }
        });
    }

    private void setPayloads(final String value) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView)findViewById(R.id.payloads)).setText("Payloads: " + value);
            }
        });
    }

    private void setP1Keys(final String value) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView)findViewById(R.id.p1keys)).setText("P1 Keys: " + value);
            }
        });
    }

    private void setP2Keys(final String value) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView)findViewById(R.id.p2keys)).setText("P2 Keys: " + value);
            }
        });
    }

    private void setP1Signatures(final String value) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView)findViewById(R.id.p1signatures)).setText("P1 Sigs: " + value);
            }
        });
    }

    private void setP2Signatures(final String value) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView)findViewById(R.id.p2signatures)).setText("P2 Sigs: " + value);
            }
        });
    }

    private void setHeadKeys(final String value) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView)findViewById(R.id.headkeys)).setText("Head Keys: " + value);
            }
        });
    }

    private void setTailKeys(final String value) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView)findViewById(R.id.tailkeys)).setText("Tail Keys: " + value);
            }
        });
    }

    private void setHeadSignatures(final String value) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView)findViewById(R.id.headsignatures)).setText("Head Sigs: " + value);
            }
        });
    }

    private void setTailSignatures(final String value) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView)findViewById(R.id.tailsignatures)).setText("Tail Sigs: " + value);
            }
        });
    }
}
