/**
 * Copyright 2014 Jorge Aliss (jaliss at gmail dot com) - twitter: @jaliss
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package service;

import securesocial.core.BasicProfile;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DemoUser implements Serializable {
    public BasicProfile main;
    public List<BasicProfile> identities;

    public DemoUser(BasicProfile user) {
        this.main = user;
        identities = new ArrayList<BasicProfile>();
        identities.add(user);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (!(o instanceof DemoUser)) {
            return false;
        }
        DemoUser user2 = (DemoUser) o;
        return user2.main.userId().equals(this.main.userId());
    }

    @Override
    public int hashCode() {
        return main.userId().hashCode();
    }
}
