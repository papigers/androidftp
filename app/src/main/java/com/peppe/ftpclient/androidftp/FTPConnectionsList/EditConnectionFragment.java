package com.peppe.ftpclient.androidftp.FTPConnectionsList;

import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import com.peppe.ftpclient.androidftp.FTPClientMain.FTPConnection;
import com.peppe.ftpclient.androidftp.FTPClientMain.MainActivity;
import com.peppe.ftpclient.androidftp.R;

public class EditConnectionFragment extends Fragment {
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_CONNECTION = "toEdit";

    public static final String TAG = "EDIT_CONNECTION_FRAG";

    private FTPConnection toEdit;

    private FormContainer form;

    private final class FormContainer {
        public TextInputLayout editName;
        public TextInputLayout editHost;
        public TextInputLayout editUser;
        public TextInputLayout editPass;
        public TextInputLayout editPort;

        public Spinner editProtocol;

        public Switch editAnonymous;

        public FormContainer(View view) {
            editName = (TextInputLayout) view.findViewById(R.id.nameInputLayout);
            editHost = (TextInputLayout) view.findViewById(R.id.hostInputLayout);
            editUser = (TextInputLayout) view.findViewById(R.id.userInputLayout);
            editPass = (TextInputLayout) view.findViewById(R.id.passInputLayout);
            editPort = (TextInputLayout) view.findViewById(R.id.portInputLayout);

            editProtocol = (Spinner) view.findViewById(R.id.editProtocolSpinner);

            editAnonymous = (Switch) view.findViewById(R.id.editAnonymousSwitch);
        }
    }


    public static EditConnectionFragment newInstance(FTPConnection param) {
        EditConnectionFragment fragment = new EditConnectionFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_CONNECTION, param);
        fragment.setArguments(args);
        return fragment;
    }

    public EditConnectionFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        if (getArguments() != null) {
            toEdit = (FTPConnection) getArguments().getSerializable(ARG_CONNECTION);
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((MainActivity) getActivity()).fab.setVisibility(View.GONE);

        form = new FormContainer(view);

        //edit connection
        if (toEdit != null) {
            if (!toEdit.getName().equals("")) {
                form.editName.getEditText().setText(toEdit.getName());
            }
            form.editHost.getEditText().setText(toEdit.getHost());

            if (toEdit.getUser().equals("anonymous")) {
                form.editAnonymous.setChecked(true);
            }
            form.editUser.getEditText().setText(toEdit.getUser());
            form.editPass.getEditText().setText(toEdit.getPass());
            form.editPort.getEditText().setText(Integer.toString(toEdit.getPort()));

            form.editProtocol.setSelection(toEdit.getIndexProtocol());
        }

        if (form.editAnonymous.isChecked()) {
            form.editUser.setVisibility(View.GONE);
            form.editPass.setVisibility(View.GONE);
        }

        form.editName.getEditText().addTextChangedListener(new FTPConnectionTextWatcher(form.editName));
        form.editHost.getEditText().addTextChangedListener(new FTPConnectionTextWatcher(form.editHost));
        form.editUser.getEditText().addTextChangedListener(new FTPConnectionTextWatcher(form.editUser));
        form.editPass.getEditText().addTextChangedListener(new FTPConnectionTextWatcher(form.editPass));
        form.editPort.getEditText().addTextChangedListener(new FTPConnectionTextWatcher(form.editPort));

        form.editAnonymous.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    form.editUser.setVisibility(View.GONE);
                    form.editPass.setVisibility(View.GONE);
                    form.editUser.getEditText().setText(R.string.anonymous_connection_user);
                    form.editPass.getEditText().setText("");
                } else {
                    form.editUser.setVisibility(View.VISIBLE);
                    form.editPass.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_connection, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_edit_connections, menu);
        //menu.findItem(R.menu.menu_main).setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_done_edit_connection:
                if (validateInput()) {
                    String name = form.editName.getEditText().getText().toString();
                    String host = form.editHost.getEditText().getText().toString();
                    String user = form.editUser.getEditText().getText().toString();
                    String pass = form.editPass.getEditText().getText().toString();
                    if (form.editAnonymous.isChecked()) {
                        user = "anonymous";
                        pass = "";
                    }

                    int port = Integer.parseInt(form.editPort.getEditText().getText().toString());
                    int protocol = form.editProtocol.getSelectedItemPosition();
                    FTPConnection edited = new FTPConnection(name, host, user, pass, port, protocol);
                    if (toEdit != null)
                        edited.setId(edited.getId());
                    ((MainActivity) getActivity()).finishEditConnection(toEdit, edited);
                } else {
                    Toast.makeText(getActivity(), "Check input errors!", Toast.LENGTH_SHORT).show();
                }
                return true;
            case android.R.id.home:
                FragmentManager fm = getActivity().getSupportFragmentManager();
                if (fm.getBackStackEntryCount() > 0) {
                    fm.popBackStack();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private boolean validateInput() {
        validateHost();
        validatePort();
        validateUser();
        return !(form.editHost.isErrorEnabled() || form.editPort.isErrorEnabled() || form.editUser.isErrorEnabled());
    }

    public void validateHost() {
        TextInputLayout til = form.editHost;
        if (til.getEditText().getText().toString().isEmpty()) {
            til.setError(getString(R.string.cn_host_edit_error));
            til.requestFocus();
        } else {
            til.setErrorEnabled(false);
        }
    }

    public void validateUser() {
        TextInputLayout til = form.editUser;
        if (til.getEditText().getText().toString().isEmpty()) {
            til.setError(getString(R.string.cn_user_edit_error));
            til.requestFocus();
        } else {
            til.setErrorEnabled(false);
        }
    }

    public void validatePort() {
        TextInputLayout til = form.editPort;
        if (til.getEditText().getText().toString().isEmpty()) {
            til.setError(getString(R.string.cn_port_edit_error));
            til.requestFocus();
        } else {
            int port = Integer.parseInt(til.getEditText().getText().toString());
            if (port < 1 || port > 65535) {
                til.setError(getString(R.string.cn_port_edit_error));
                til.requestFocus();
            } else
                til.setErrorEnabled(false);
        }
    }

    private class FTPConnectionTextWatcher implements TextWatcher {
        private TextInputLayout til;

        public FTPConnectionTextWatcher(TextInputLayout til) {
            this.til = til;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            switch (til.getId()) {
                case R.id.hostInputLayout:
                    validateHost();
                    break;
                case R.id.userInputLayout:
                    validateUser();
                    break;
                case R.id.portInputLayout:
                    validatePort();
                    break;
            }
        }
    }

}
