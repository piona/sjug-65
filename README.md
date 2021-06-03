# Karty Java Cards

Repozytorium zawiera materiały ze spotkania [SJUG #65](https://www.meetup.com/pl-PL/Silesia-JUG/events/278231699/).

Wersja zainstalowanych narzędzi zależy od posiadanej karty lub od symulatora
jaki ma być używany. Podczas spotkania używałem Java Card Development Kit 2.2.2.

Wykorzystywane narzędzia:

- kompilator Java w wersji 8
- Java Card Development Kit Tools:
  <https://www.oracle.com/java/technologies/javacard-sdk-downloads.html/>
  (biblioteki wykorzystywane podczas kompilacji i narzędzia do konwersji apletu)
- skrypty do budowania apletu i aplikacji są utworzone w
  [Apache Ant](https://ant.apache.org/)
- w przypadku rzeczywistej karty należy uruchomić menadżera PC/SC (w Linux jest
  to demon `pcscd`) oraz zainstalować czytnik
- narzędzie do ładowania apletów do kart
  [GlobalPlatformPro](https://github.com/martinpaljak/GlobalPlatformPro)

Z poziomu linii poleceń powinien być dostępny kompilator i `ant`. Należy ustawić
zmienną środowiskową `JC_HOME` wskazując katalog, gdzie zainstalowane zostało
Java Card Development Kit. Jako alias do wywołania GlobalPlatformPro można
zdefiniować polecenie `gp-pro` (takie używane jest w instrukcji poniżej).

Używając rzeczywistej karty należy zachować ostrożność przy ładowaniu i usuwaniu
apletu. Domyślne skrypty zakładają, że Card Manager wykorzystuje klucze
o wartości `404142434445464748494a4b4c4d4e4f`.

## Budowanie apletu

Przykładowy aplet jest wyłącznie kodem demonstracyjnym. Dla zachowania
przejrzystości kodu usunięto z niego niezbędne sprawdzenia np. poprawności
odebranych/przechowywanych danych czy kolejności wykonywanych komend.
Zastosowane zostały klucze RSA o długości 512 ponieważ symulator `cref`
wspiera wyłącznie klucze tej długości. W systemie produkcyjnym należy używać
kluczy o długości co najmniej 2048 bit (karta powinna wspierać operacje na
takich długościach kluczy).

Kod przykładowego apletu znajduje się w katalogu `jcapplet`. Parametry
budowanego apletu (AID pakietu, apletu) zawarte są w pliku `build.properties`.

Używając środowiska Java Card w wersji 3 należy w skrypcie `build.xml` zmienić
narzędzie do konwersji apletu z `com.sun.javacard.converter.Converter` na
`com.sun.javacard.converter.Main`.

Aby zbudować aplet wywołujemy

    $ ant build

Aby usunąć zbudowany aplet wraz ze skryptami wywołujemy

    $ ant clean

### Wgranie apletu do karty

Przed wgraniem apletu można sprawdzić jakie aplety są już w karcie, a przy
okazji zweryfikować czy używamy prawidłowych kluczy dla apletu Card Manager.
Odpowiednie skrypty znajdują się w katalogu `out`. Mogą one wymagać ustawienia
dodatkowych opcji jak np. wybór czytnika PC/SC.

Lista apletów

    $ source jcapplet-list.gp

Wgranie apletu do karty (usuwa aplet jeśli istnieje i ładuje go ponownie)

    $ source jcapplet-load.gp

Usunięcie apletu (usuwa instancję apletu i pakiet)

    $ source jcapplet-delete.gp

### Wgranie apletu do symulatora

Aby wgrać aplet do symulatora należy użyć w nim kluczy RSA o długości 512 bit.
W kodzie apletu należy wybrać określony typ klucza.

Uruchamiamy skrypt `cref-loop.sh` (lub `cref-loop.bat`). Włączy on symulator
i będzie zapamiętywał jego stan w pliku `javacard.eeprom`. Skrypt ma na celu
cykliczne uruchamianie symulatora ponieważ kończy on swoje działanie po
odłączeniu od karty.

Po uruchomieniu symulatora wywołujemy

    $ ant load

Stan karty można wyczyścić poprzez zatrzymanie skryptu i usunięcie pliku
`javacard.eeprom`.

## Aplikacja używająca karty

Kod przykładowej aplikacji korzystającej z apletu znajduje się w katalogu
`jcterminal`.

Do połączenia z symulatorem wykorzystywany jest sterownik pochodzący z projektu
[SCUBA](http://scuba.sourceforge.net/). W aplikacji należy wybrać określony
sposób łączenia z terminalem.

Budowanie i uruchomienie aplikacji

    $ ant build
    $ ant run

