/*
 * Copyright © 2015 Integrated Knowledge Management (support@ikm.dev)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.ikm.tinkar.common.id;

import dev.ikm.tinkar.common.id.impl.PublicIdCollections;

import java.util.List;

public class PublicIdListFactory {
    public static final PublicIdListFactory INSTANCE = new PublicIdListFactory();

    public PublicIdList empty()
    {
        return PublicIdCollections.ListN.EMPTY_LIST;
    }

    public PublicIdList of()
    {
        return this.empty();
    }

    public PublicIdList of(PublicId one)
    {
        return new PublicIdCollections.List12(one);
    }

    public PublicIdList of(PublicId one, PublicId two)
    {
        return new PublicIdCollections.List12(one, two);
    }

    public PublicIdList of(PublicId one, PublicId two, PublicId three)
    {
        return new PublicIdCollections.ListN<>(one, two, three);
    }

     public PublicIdList of(PublicId one, PublicId two, PublicId three, PublicId four)
    {
        return new PublicIdCollections.ListN<>(one, two, three, four);
    }

    public PublicIdList of(PublicId one, PublicId two, PublicId three, PublicId four, PublicId five)
    {
        return new PublicIdCollections.ListN<>(one, two, three, four, five);
    }

    public PublicIdList of(PublicId one, PublicId two, PublicId three, PublicId four, PublicId five, PublicId six)
    {
        return new PublicIdCollections.ListN<>(one, two, three, four, five, six);
    }

    public PublicIdList of(PublicId one, PublicId two, PublicId three, PublicId four, PublicId five, PublicId six, PublicId seven)
    {
        return new PublicIdCollections.ListN<>(one, two, three, four, five, six, seven);
    }


    public PublicIdList of(PublicId one, PublicId two, PublicId three, PublicId four, PublicId five, PublicId six, PublicId seven, PublicId eight)
    {
        return new PublicIdCollections.ListN<>(one, two, three, four, five, six, seven, eight);
    }

    public PublicIdList of(PublicId one, PublicId two, PublicId three, PublicId four, PublicId five, PublicId six, PublicId seven, PublicId eight, PublicId nine)
    {
        return new PublicIdCollections.ListN<>(one, two, three, four, five, six, seven, eight, nine);
    }

    public PublicIdList of(PublicId one, PublicId two, PublicId three, PublicId four, PublicId five, PublicId six, PublicId seven, PublicId eight, PublicId nine, PublicId ten)
    {
        return new PublicIdCollections.ListN<>(one, two, three, four, five, six, seven, eight, nine, ten);
    }
    public PublicIdList ofArray(PublicId[] items) {
        return of(items);
    }

    public PublicIdList of(PublicId... items)
    {
        if (items == null || items.length == 0)
        {
            return this.empty();
        }

        switch (items.length)
        {
            case 1:
                return this.of(items[0]);
            case 2:
                return this.of(items[0], items[1]);
            case 3:
                return this.of(items[0], items[1], items[2]);
            case 4:
                return this.of(items[0], items[1], items[2], items[3]);
            case 5:
                return this.of(items[0], items[1], items[2], items[3], items[4]);
            case 6:
                return this.of(items[0], items[1], items[2], items[3], items[4], items[5]);
            case 7:
                return this.of(items[0], items[1], items[2], items[3], items[4], items[5], items[6]);
            case 8:
                return this.of(items[0], items[1], items[2], items[3], items[4], items[5], items[6], items[7]);
            case 9:
                return this.of(items[0], items[1], items[2], items[3], items[4], items[5], items[6], items[7], items[8]);
            case 10:
                return this.of(items[0], items[1], items[2], items[3], items[4], items[5], items[6], items[7], items[8], items[9]);

            default:
                return new PublicIdCollections.ListN<>(items);
        }
    }

    private PublicIdList of(List<PublicId> items)
    {
        switch (items.size())
        {
            case 0:
                return this.empty();
            case 1:
                return this.of(items.get(0));
            case 2:
                return this.of(items.get(0), items.get(1));
            case 3:
                return this.of(items.get(0), items.get(1), items.get(2));
            case 4:
                return this.of(items.get(0), items.get(1), items.get(2), items.get(3));
            case 5:
                return this.of(items.get(0), items.get(1), items.get(2), items.get(3), items.get(4));
            case 6:
                return this.of(items.get(0), items.get(1), items.get(2), items.get(3), items.get(4), items.get(5));
            case 7:
                return this.of(items.get(0), items.get(1), items.get(2), items.get(3), items.get(4), items.get(5), items.get(6));
            case 8:
                return this.of(items.get(0), items.get(1), items.get(2), items.get(3), items.get(4), items.get(5), items.get(6), items.get(7));
            case 9:
                return this.of(items.get(0), items.get(1), items.get(2), items.get(3), items.get(4), items.get(5), items.get(6), items.get(7), items.get(8));
            case 10:
                return this.of(items.get(0), items.get(1), items.get(2), items.get(3), items.get(4), items.get(5), items.get(6), items.get(7), items.get(8), items.get(9));

            default:
                return new PublicIdCollections.ListN<>(items.toArray(new PublicId[items.size()]));
        }
    }

}
