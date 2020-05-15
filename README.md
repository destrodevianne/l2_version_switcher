# l2_version_switcher

![L2smr Screenshot](images/screenshot.png)

Серверы:
Код:
Server      Host                            Game        Version(05.01.16)
NC West     lineage2.patcher.ncsoft.com     LINEAGE2    1(Lindvior 533)
                                                        2(557)
                                                        5(558)
                                                        13(Valiance 575)
                                                        15(578)
                                                        18(580)
                                                        23(581)
                                                        30(583)
                                                        34(Ertheia 603)
                                                        42(606)
                                                        60(Shadows of Light 24)
                                                        73(New Guardian of Astatine 28)
                                                        80
Korean PTS  l2_kor_test.up4rep.plaync.co.kr L2_KOR_TEST 48(Lindvior 526)
                                                        55(528)
                                                        58(Epeisodion 555)
                                                        60(558)
                                                        61(559)
                                                        68(560)
                                                        71(563)
                                                        79(566)
                                                        87(567)
                                                        88(568)
                                                        92(Dimensional Strangers 596)
                                                        96(597)
                                                        111(598)
                                                        115(599)
                                                        124(Classic 19)
                                                        146(The Beginning of Journey 21)
                                                        161(Classic: Age of War 22)
                                                        164(Shadows of Light(?) 24)
                                                        168(25)
                                                        178(New Guardian of Astatine(?) 27)
                                                        184(28)
                                                        187(Hymn of the Soul(?) 39)
                                                        194(40)
                                                        200(Will of the Ancients(?) 43)
                                                        207(Helios Lord of Bifrost 55)
                                                        214(57)
                                                        219(64)
                                                        224
Запуск:
Код:
java -jar l2_version_switcher.jar host game version <filename_filter>
Пример:
Выкачивает систем линдвиора(533)
Код:
java -jar l2_version_switcher.jar lineage2.patcher.ncsoft.com LINEAGE2 1 "system\*"
