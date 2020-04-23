package ch.epfl.sdp.kandle.exceptions;

import ch.epfl.sdp.kandle.Kandle;
import ch.epfl.sdp.kandle.R;

public class IncompleteDataException extends Exception {

    public IncompleteDataException() {
        super(Kandle.getContext().getResources().getString(R.string.incomplete_data));
    }
}

