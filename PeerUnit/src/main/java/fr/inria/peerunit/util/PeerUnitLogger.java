/*
This file is part of PeerUnit.

PeerUnit is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

PeerUnit is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with PeerUnit.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.inria.peerunit.util;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The application log class. This class is used for  log application runtime events.
 * This class's objects are parametrized by the values of properties of application properties file.
 *
 * @author Eduardo Almeida
 * @author Aboubakar Koïta
 * @version 1.0
 * @since 1.0
 */
public class PeerUnitLogger {

    public static void createLogger(TesterUtil defaults, String pattern) throws IOException {
        String folder = defaults.getLogFolder();
        File f = new File(folder);
        f.mkdirs();
        Level l = defaults.getLogLevel();
        FileHandler handler = new FileHandler(folder + "/" + pattern);

        handler.setFormatter(new LogFormat());
        handler.setLevel(l);

        Logger myLogger = Logger.getLogger("fr.inria");
        myLogger.setUseParentHandlers(false);
        myLogger.addHandler(handler);
        myLogger.setLevel(l);
    }
}
