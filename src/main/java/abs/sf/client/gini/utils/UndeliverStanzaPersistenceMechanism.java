package abs.sf.client.gini.utils;

import java.io.IOException;
import java.util.List;

import abs.ixi.client.io.UndeliveredStanzaManager;
import abs.ixi.client.util.CollectionUtils;
import abs.ixi.client.xmpp.packet.Stanza;
import abs.sf.client.gini.db.DbManager;

public class UndeliverStanzaPersistenceMechanism implements UndeliveredStanzaManager.PersistenceMechanism {
	@Override
	public void write(Stanza stanza) throws IOException {
		DbManager.getInstance().persistUndeliverStanza(stanza);
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
		if (stanzaCount > 0) {
			DbManager.getInstance().deleteFirstUndeliveredStanza(stanzaCount);
		}
	}

	@Override
	public List<Stanza> readAll() throws IOException {
		return DbManager.getInstance().fetchAllUndeliverStanzas();
	}

	@Override
	public void truncateUndeliverStanzas() {
		DbManager.getInstance().deleteAllUndeliverStanzas();
	}

}
