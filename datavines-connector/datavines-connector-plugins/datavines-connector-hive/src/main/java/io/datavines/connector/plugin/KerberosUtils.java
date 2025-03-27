/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.datavines.connector.plugin;

import io.datavines.common.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.kerby.kerberos.kerb.keytab.Keytab;
import org.apache.kerby.kerberos.kerb.type.base.PrincipalName;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Slf4j
public class KerberosUtils {

    public static final String JAVA_SECURITY_KRB5_CONF = "java.security.krb5.conf";
    public static final String HADOOP_SECURITY_AUTHENTICATION = "hadoop.security.authentication";
    public static final String KERBEROS = "kerberos";

    public static boolean checkKerberosConfig(String keytabPrincipal, String keytabPath, String krb5Config){
        if(StringUtils.isEmptyOrNullStr(keytabPrincipal)){
            return false;
        }
        if(StringUtils.isEmptyOrNullStr(keytabPath)){
            return false;
        }
        if(StringUtils.isEmptyOrNullStr(krb5Config)){
            return false;
        }
        return true;
    }

    public static List<String> getPrincipalListFromKeytab(String keytabPath) throws IOException {
        Keytab keytab = Keytab.loadKeytab(new File(keytabPath));
        List<PrincipalName> principalNameList = keytab.getPrincipals();
        List<String> principalList = new ArrayList<>();
        for (PrincipalName principalName : principalNameList) {
            log.info("from keytab: {}, find principalName: {}", keytabPath, principalName);
            principalList.add(principalName.getName());
        }
        return principalList;
    }

    public static boolean initKerberos(String keytabPrincipal, String keytabPath, String krb5Config){
        boolean kerberosInit = false;
        try {
            Configuration conf = new Configuration();
            System.setProperty(JAVA_SECURITY_KRB5_CONF, krb5Config);
            conf.set(HADOOP_SECURITY_AUTHENTICATION, KERBEROS);
            UserGroupInformation.setConfiguration(conf);
            UserGroupInformation.loginUserFromKeytab(keytabPrincipal, keytabPath);
            kerberosInit = true;
        } catch (IOException e) {
            log.error("initKerberos error", e);
        }
        return kerberosInit;
    }
}
