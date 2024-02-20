package pl.tt.storagemanager.storagemanager.exception;

public class AnyStorageInstanceNotAvailableException extends RuntimeException {

	private static final String MESSAGE = "Any storage instance not available";
	public AnyStorageInstanceNotAvailableException() {
		super(MESSAGE);
	}
}
