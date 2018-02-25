package network.xyo.client;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.security.InvalidKeyException;

import network.xyo.sdk.nodes.Node;

/**
 * A fragment representing a single Node detail screen.
 * This fragment is either contained in a {@link NodeListActivity}
 * in two-pane mode (on tablets) or a {@link NodeDetailActivity}
 * on handsets.
 */
public class NodeDetailFragment extends Fragment {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String NODE_NAME = "node_name";

    /**
     * The dummy content this fragment is presenting.
     */
    private Node node;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public NodeDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(NodeDetailFragment.NODE_NAME)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            node = Node.get(getArguments().getString(NodeDetailFragment.NODE_NAME));
        } else {
            throw new RuntimeException();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.node_detail, container, false);

        Button ledger = rootView.findViewById(R.id.ledger);

        ledger.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = NodeDetailFragment.this.getContext();
                Intent intent = new Intent(context, LedgerActivity.class);
                intent.putExtra(NODE_NAME, node.getName() );

                context.startActivity(intent);
            }
        });

        return rootView;
    }
}
