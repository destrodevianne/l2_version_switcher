/*
 * Copyright (c) 2016 acmi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package acmi.l2.clientmod.l2_version_switcher;

import org.apache.commons.io.IOCase;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static org.apache.commons.io.FilenameUtils.separatorsToSystem;
import static org.apache.commons.io.FilenameUtils.wildcardMatch;

public class Main {
    public static void main(String[] args) {
        if (args.length != 3 && args.length != 4) {
            System.out.println("USAGE: l2_version_switcher.jar host game version <filter>");
            System.out.println("EXAMPLE: l2_version_switcher.jar " + L2.NCWEST_HOST + " " + L2.NCWEST_GAME + " 1 \"system\\*\"");
            System.out.println("         l2_version_switcher.jar " + L2.PLAYNC_TEST_HOST + " " + L2.PLAYNC_TEST_GAME + " 48");
            System.exit(0);
        }

        String host = args[0];
        String game = args[1];
        int version = Integer.parseInt(args[2]);
        String filter = args.length == 4 ? separatorsToSystem(args[3]) : null;
        Helper helper = new Helper(host, game, version);
        boolean available = false;

        try {
            available = helper.isAvailable();
        } catch (IOException e) {
            System.err.print(e.getClass().getSimpleName());
            if (e.getMessage() != null) {
                System.err.print(": " + e.getMessage());
            }

            System.err.println();
        }

        System.out.println(String.format("Version %d available: %b", version, available));
        if (!available) {
            System.exit(0);
        }

        List<FileInfo> fileInfoList = null;
        try {
            fileInfoList = helper.getFileInfoList();
        } catch (IOException e) {
            System.err.println("Couldn\'t get file info map");
            System.exit(1);
        }

        File l2Folder = new File(System.getProperty("user.dir"));
        List<FileInfo> toUpdate = fileInfoList
                .parallelStream()
                .filter(fi -> {
                    String filePath = separatorsToSystem(fi.getPath());

                    if (filter != null && !wildcardMatch(filePath, filter, IOCase.INSENSITIVE))
                        return false;
                    File file = new File(l2Folder, filePath);

                    try {
                        if (file.exists() && file.length() == fi.getSize() && Util.hashEquals(file, fi.getHash())) {
                            System.out.println(filePath + ": OK");
                            return false;
                        }
                    } catch (IOException e) {
                        System.out.println(filePath + ": couldn't check hash: " + e);
                        return true;
                    }

                    System.out.println(filePath + ": need update");
                    return true;
                })
                .collect(Collectors.toList());

        List<String> errors = Collections.synchronizedList(new ArrayList<>());
        ExecutorService executor = Executors.newFixedThreadPool(16);
        CompletableFuture[] tasks = toUpdate
                .stream()
                .map(fi -> CompletableFuture.runAsync(() -> {
                    String filePath = separatorsToSystem(fi.getPath());
                    File file = new File(l2Folder, filePath);

                    File folder = file.getParentFile();
                    if (!folder.exists()) {
                        if (!folder.mkdirs()) {
                            errors.add(filePath + ": couldn't create parent dir");
                            return;
                        }
                    }

                    try (InputStream input = Util.getUnzipStream(new BufferedInputStream(helper.getDownloadStream(fi.getPath())));
                         OutputStream output = new BufferedOutputStream(new FileOutputStream(file))) {
                        byte[] buffer = new byte[Math.min(fi.getSize(), 1 << 24)];
                        int pos = 0;
                        int r;
                        while ((r = input.read(buffer, pos, buffer.length - pos)) >= 0) {
                            pos += r;
                            if (pos == buffer.length) {
                                output.write(buffer, 0, pos);
                                pos = 0;
                            }
                        }
                        if (pos != 0) {
                            output.write(buffer, 0, pos);
                        }
                        System.out.println(filePath + ": OK");
                    } catch (IOException e) {
                        String msg = filePath + ": FAIL: " + e.getClass().getSimpleName();
                        if (e.getMessage() != null) {
                            msg += ": " + e.getMessage();
                        }
                        errors.add(msg);
                    }
                }, executor))
                .toArray(CompletableFuture[]::new);
        CompletableFuture
                .allOf(tasks)
                .thenRun(() -> {
                    for (String err : errors)
                        System.err.println(err);
                    executor.shutdown();
                });
    }
}
