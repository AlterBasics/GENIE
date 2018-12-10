package abs.sf.client.genie.utils;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import abs.ixi.client.io.UndeliveredStanzaManager;
import abs.ixi.client.util.CollectionUtils;
import abs.ixi.client.xmpp.packet.Stanza;
import abs.sf.client.genie.db.DbManager;
import abs.sf.client.genie.db.exception.DbException;

public class UndeliverStanzaPersistenceMechanism implements UndeliveredStanzaManager.PersistenceMechanism {
	private static final Logger LOGGER = Logger.getLogger(UndeliverStanzaPersistenceMechanism.class.getName());

	@Override
	public void write(Stanza stanza) throws IOException {
		try {

			DbManager.getInstance().persistUndeliverStanza(stanza);

		} catch (DbException e) {
			LOGGER.log(Level.WARNING, "Failed to write stanza", e);
			throw new IOException(e);
		}
	}

	@Override
	public void write(List<Stanza> stanzas) throws IOException {
		if (!CollectionUtils.isNullOrEmpty(stanzas)) {
			for (Stanza stanza : stanzas) {
				this.write(stanza);
			}
		}
	}

	@Override
	public void remove(int stanzaCount) throws IOException {
		try {
			if (stanzaCount > 0) {
				DbManager.getInstance().deleteFirstUndeliveredStanza(stanzaCount);
			}
		} catch (DbException e) {
			LOGGER.log(Level.WARNING, "Failed to remove stanza", e);
			throw new IOException(e);
		}

	}

	@Override
	public List<Stanza> readAll() throws IOException {
		try {
			return DbManager.getInstance().fetchAllUndeliverStanzas();
		} catch (DbException e) {
			LOGGER.log(Level.WARNING, "Failed to read stanza ", e);
			throw new IOException(e);
		}
	}

	@Override
	public void truncateUndeliverStanzas() {
		try {
			DbManager.getInstance().deleteAllUndeliverStanzas();
		} catch (DbException e) {
			LOGGER.log(Level.WARNING, "Failed to t stanza ", e);
		}

	}

}
