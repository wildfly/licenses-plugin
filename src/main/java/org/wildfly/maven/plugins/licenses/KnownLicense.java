package org.wildfly.maven.plugins.licenses;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.model.License;

public enum KnownLicense {
    APACHE_1_1("Apache License 1.1", "https://www.apache.org/licenses/LICENSE-1.1",
            "apache software license, version 1.1"),
    APACHE_2_0("Apache License 2.0", "https://www.apache.org/licenses/LICENSE-2.0",
            "apache 2",
            "apache 2.0 license",
            "apache 2.0",
            "apache license v2.0",
            "apache license version 2.0",
            "apache license, version 2.0",
            "apache software license, version 2.0",
            "apache-2.0",
            "asl 2.0",
            "the apache license, version 2.0",
            "the apache software license, version 2.0"),

    BSD_2_CLAUSE("BSD 2-clause \"Simplified\" License", "https://opensource.org/license/BSD-2-Clause/",
            "bsd-2-clause",
            "bsd 2-clause license",
            "the bsd 2-clause license"),

    BSD_3_CLAUSE("BSD 3-Clause \"New\" or \"Revised\" License", "https://www.opensource.org/licenses/BSD-3-Clause",
            "3-clause bsd license",
            "bsd license 3",
            "bsd-3-clause"),

    CPL("Common Public License 1.0", "https://www.eclipse.org/legal/cpl-v10.html", "cpl"),

    CC0("Creative Commons Zero v1.0 Universal", "https://creativecommons.org/publicdomain/zero/1.0/legalcode",
            "cc0",
            "public domain, per creative commons cc0"),

    CC_2_5("Creative Commons Attribution 2.5", "https://creativecommons.org/licenses/by/2.5/legalcode",
            "creative commons attribution license 2.5"),

    EDL_1_0("Eclipse Distribution License, Version 1.0", "https://repository.jboss.org/licenses/edl-1.0.txt",
            "eclipse distribution license - v 1.0",
            "eclipse distribution license v. 1.0",
            "eclipse distribution license (new bsd license)",
            "edl 1.0"),

    EPL_1_0("Eclipse Public License 1.0", "https://repository.jboss.org/licenses/epl-1.0.txt",
            "eclipse public license v1.0",
            "eclipse public license - v 1.0",
            "epl 1.0"),

    EPL_2_0("Eclipse Public License 2.0", "https://www.eclipse.org/legal/epl-v20.html",
            "eclipse public license - v 2.0",
            "eclipse public license v. 2.0",
            "eclipse public license v2.0",
            "epl 2.0",
            "epl-2.0"),

    FSF_ALL("FSF All Permissive License", "https://www.gnu.org/prep/maintain/html_node/License-Notices-for-Other-Files.html"),

    GPL_2_0_W_CPE("GNU General Public License v2.0 only, with Classpath exception",
            "https://fedoraproject.org/wiki/Licensing/GPL_Classpath_Exception",
            "gnu general public license (gpl), version 2, with the classpath exception",
            "gnu general public license, version 2 with the classpath exception",
            "gnu general public license, version 2 with the gnu classpath exception",
            "gpl-2.0-with-classpath-exception",
            "gpl2 w/ cpe"),

    LGPL_2_0_ONLY("GNU Library General Public License v2 only", "https://www.gnu.org/licenses/old-licenses/lgpl-2.0-standalone.html"),

    LGPL_2_1_ONLY("GNU Lesser General Public License v2.1 only",
            "https://www.gnu.org/licenses/old-licenses/lgpl-2.1.html",
            "lgpl 2.1",
            "the gnu lesser general public license, version 2.1",
            "lgpl, version 2.1"),

    LGPL_2_1_LATER("GNU Lesser General Public License v2.1 or later",
            "https://www.gnu.org/licenses/old-licenses/lgpl-2.1-standalone.html",
            "gnu library general public license v2.1 or later",
            "lgpl 2.1 or later",
            "lgpl-2.1-or-later"),

    LGPL_3_0_ONLY("GNU Lesser General Public License v3.0 only", "https://www.gnu.org/licenses/lgpl-3.0-standalone.html"),

    LGPL_3_0_LATER("GNU Lesser General Public License v3.0 or later", "https://spdx.org/licenses/LGPL-3.0+.html",
            "lesser general public license, version 3 or greater"),

    INDIANA_UNIVERSITY_EXTREME("Indiana University Extreme! Lab Software License 1.1.1",
            "https://enterprise.dejacode.com/licenses/public/indiana-extreme/?_list_filters=q%3Dindiana%2Bextreme#license-text"),

    MIT_0("MIT-0", "https://spdx.org/licenses/MIT-0.html"),

    MIT("MIT License", "https://www.opensource.org/licenses/MIT",
            "bouncy castle licence",
            "mit",
            "the mit license"),

    MPL_1_1("Mozilla Public License 1.1", "https://www.mozilla.org/MPL/MPL-1.1.html",
            "mpl 1.1"),

    MPL_2_0("Mozilla Public License 2.0", "https://fedoraproject.org/wiki/Licensing/MPLv2.0",
            "mpl 2.0"),

    PUBLIC_DOMAIN("Public Domain", null);

    public final String name;
    public final List<String> aliases;
    public final License license;

    KnownLicense(final String name, String url, String... aliases) {
        this.name = name;
        this.aliases = List.of(aliases);
        this.license = createLicense(name, url);
    }

    public static Map<String, License> toMap() {
        Map<String, License> licenses = new HashMap<>();
        Arrays.stream(KnownLicense.values()).forEach(k -> {
            licenses.put(k.name.toLowerCase(), k.license);
            k.aliases.forEach(a -> licenses.put(a.toLowerCase(), k.license));
        });

        return Collections.unmodifiableMap(licenses);
    }

    private static License createLicense(String name, String url) {
        License license = new License();
        license.setName(name);
        license.setUrl(url);
        return license;
    }
}
