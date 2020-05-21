package ch.epfl.sdp.kandle.exceptions;

import ch.epfl.sdp.kandle.Kandle;
import ch.epfl.sdp.kandle.R;

public class NoInternetException extends Exception {

    public NoInternetException() {
        super(Kandle.getContext().getResources().getString(R.string.noConnexion));
    }
}
