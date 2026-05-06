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
package dev.ikm.komet.framework;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.sparrowwallet.toucan.LifeHash;
import com.sparrowwallet.toucan.LifeHashVersion;
import dev.ikm.tinkar.common.id.PublicId;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Identicon {

    // Limit concurrency to avoid CPU thrashing during rapid scrolling
    private static final ExecutorService generationExecutor = Executors.newFixedThreadPool(
            Math.max(2, Runtime.getRuntime().availableProcessors() - 1),
            r -> {
                Thread t = new Thread(r);
                t.setDaemon(true);
                t.setName("Identicon-Generator");
                return t;
            }
    );

    /**
     * Maximum number of cached identicons. Default 2,048 entries
     * (≈128 MB at 128×128 ARGB per image). Override via system property
     * {@code ike.identicon.cache.size} for memory-constrained deployments.
     * Caffeine evicts least-recently-used entries when the cache is full;
     * misses cost a single async generation, same as the pre-cache behavior,
     * so being conservative on size has bounded downside.
     */
    private static final int CACHE_MAX_SIZE =
            Integer.getInteger("ike.identicon.cache.size", 2_048);

    /**
     * LRU cache of generated identicon images keyed by source {@link PublicId}.
     * The {@link WritableImage} stored here is the same instance returned to
     * every caller for that publicId. The first lookup for an unseen publicId
     * gets a fresh transparent placeholder which is filled asynchronously by
     * {@link #generationExecutor}; once filled, the image stays in the cache,
     * and every subsequent lookup returns it synchronously — no flicker, no
     * regeneration, no executor work.
     *
     * <p>Cell-reuse-driven scroll churn (search panels, list views, navigation
     * trees) was the original motivation: every cell rebind hits the cache,
     * so an identicon that was once on screen and scrolled back to comes back
     * instantly without a transparent flash.
     */
    private static final Cache<PublicId, Image> IMAGE_CACHE = Caffeine.newBuilder()
            .maximumSize(CACHE_MAX_SIZE)
            .build();

    /*
    https://github.com/bryc/code/wiki/Identicons

    https://barro.github.io/2018/02/avatars-identicons-and-hash-visualization/

    https://www.puls200.de/?p=316

    https://web.archive.org/web/20200430104323/http://scott.sherrillmix.com:80/blog/blogger/wp_identicon/

    https://blog.codinghorror.com/identicons-for-net/

    http://web.archive.org/web/20070208033427/http://www.docuverse.com/blog/donpark/2004/05/03/secure-ui-9-block-phishmarks

    http://www.levitated.net/daily/lev9block.html

    https://github-wiki-see.page/m/bryc/code/wiki/Identicons

    https://github.com/davidhampgonsalves/Contact-Identicons

     */

    public static ImageView generateIdenticon(PublicId publicId, int image_width, int image_height) {

        Image identicon = generateIdenticonImage(publicId);

        ImageView finalImageView = new ImageView(identicon);
        finalImageView.setFitWidth(image_width);
        finalImageView.setFitHeight(image_height);

        return finalImageView;
    }

    /**
     * Get the identicon image for a {@link PublicId}, returning a cached
     * instance if available. First lookup for an unseen publicId returns a
     * transparent placeholder that fills asynchronously via
     * {@link #generationExecutor}; subsequent lookups for the same publicId
     * return the same instance synchronously.
     *
     * <p>The cache is bounded to {@link #CACHE_MAX_SIZE} entries (LRU
     * eviction). The cost of a cache miss is one async generation — the same
     * cost every call paid before this cache existed.
     *
     * <p>Concurrency: Caffeine's {@code get(key, mappingFunction)} runs the
     * mapping function at most once per absent key, so concurrent callers for
     * the same publicId share one {@link WritableImage} and one async
     * generation.
     *
     * @param publicId the source identifier
     * @return the cached identicon image (may still be filling the first time
     *         it's requested for a given {@code publicId})
     */
    public static Image generateIdenticonImage(PublicId publicId) {
        return IMAGE_CACHE.get(publicId, Identicon::createAndFillAsync);
    }

    /**
     * Caffeine mapping function: build a transparent 128×128 placeholder, kick
     * off the async LifeHash generation that fills it later, and return the
     * placeholder. The cache stores the placeholder; once the async fill
     * completes via {@link Platform#runLater}, the same instance is now the
     * fully-rendered identicon and any {@link ImageView} bound to it repaints
     * automatically (JavaFX observes pixel-buffer changes on
     * {@link WritableImage}).
     */
    private static Image createAndFillAsync(PublicId publicId) {
        int size = 128;
        WritableImage progressiveImage = new WritableImage(size, size);
        CompletableFuture.runAsync(() -> {
            Image result = generateIdenticonImageLifeHash(publicId, LifeHashVersion.VERSION2);
            int w = (int) result.getWidth();
            int h = (int) result.getHeight();
            int[] scaledPixels = new int[size * size];
            PixelReader reader = result.getPixelReader();
            // Nearest-neighbor scaling to the cache's 128×128 target.
            for (int y = 0; y < size; y++) {
                int sourceY = Math.min(y * h / size, h - 1);
                for (int x = 0; x < size; x++) {
                    int sourceX = Math.min(x * w / size, w - 1);
                    scaledPixels[y * size + x] = reader.getArgb(sourceX, sourceY);
                }
            }
            Platform.runLater(() -> progressiveImage.getPixelWriter().setPixels(
                    0, 0, size, size,
                    PixelFormat.getIntArgbInstance(),
                    scaledPixels, 0, size));
        }, generationExecutor);
        return progressiveImage;
    }

    /**
     * Generates an identicon based on a publicID using the Lifehash algorithm.
     * The Lifehash algorithm has different modes of operation which you can choose from by passing a different LifeHashVersion instanece.
     *
     * @param publicId the public id which would be the basis for generating the identicon. Different publicids will generate different identicons.
     * @param lifeHashVersion the LifehashVersion to use. Different versions will produce different images.
     * @return the generated Identicon image.
     */
    private static Image generateIdenticonImageLifeHash(PublicId publicId, LifeHashVersion lifeHashVersion) {
        LifeHash.Image lifeHashImage = LifeHash.makeFromUTF8(publicId.idString(), lifeHashVersion, 1, false);

        WritableImage writableImage = new WritableImage(lifeHashImage.width(), lifeHashImage.height());
        PixelWriter pixelWriter = writableImage.getPixelWriter();
        for (int y = 0; y < lifeHashImage.height(); y++) {
            for (int x = 0; x < lifeHashImage.width(); x++) {
                int offset = (y * lifeHashImage.width() + x) * (lifeHashImage.hasAlpha() ? 4 : 3);
                int r = lifeHashImage.colors().get(offset) & 0xFF;
                int g = lifeHashImage.colors().get(offset + 1) & 0xFF;
                int b = lifeHashImage.colors().get(offset + 2) & 0xFF;
                if(lifeHashImage.hasAlpha()) {
                    double a = (lifeHashImage.colors().get(offset + 3) & 0xFF) / 255.0;
                    pixelWriter.setColor(x, y, Color.rgb(r, g, b, a));
                } else {
                    pixelWriter.setColor(x, y, Color.rgb(r, g, b));
                }
            }
        }
        return writableImage;
    }
}
