program testAdv;
var i, j, k: integer;
    ca, cb: char;
    ba, bb, bt, bf: boolean;
    a1, a2, a3, a4, a5, a6, a7, a8, a9: integer;
    b1, b2, b3, b4, b5, b6, b7, b8, b9: integer;
    s: array[1..9] of char;

begin
    i := 6;
    j := -7;
    k := -8;

    a1 := 3 + j + 5;
    a2 := 3 + i + 2 - j + k - 8;
    if j < i then begin
        a3 := 3;
    end;
    if k <= i - 7 then begin
        a4 := 4;
    end;
    if -k - 2 = 6 then begin
        a5 := 5;
    end;
    if (i > j) then begin
        a6 := 6;
    end;
    if i >= k + 7 then begin
        a7 := 7;
    end;
    if -k + 2 <> 5 then begin
        a8 := 8;
    end;
    if -i*j+j*1+(-2)*i-((-i div 3 -j/7+(-k)mod(-((1+(2+(k+i)))-2)))) = 24 then begin
        a9 := 9;
    end;

    writeln(a1, a2, a3, a4, a5, a6, a7, a8, a9, ' (integer test)');


    ca := 'A';
    cb := 'b';
    bt := true;
    bf := false;

    ba := j < i;
    if ba = (k < 0) then begin
        b1 := 1;
    end;
    if true <= ba then begin
        b2 := 2;
    end;
    if bf < bt then begin
        b3 := 3;
    end;
    if not (not (ba and bt) or bf) then begin
        b4 := 4;
    end;
    if (true or false) and (true <> false) then begin
        b5 := 5;
    end;
    if ca <= cb then begin
        b6 := 6;
    end;
    if ca = 'A' then begin
        b7 := 7;
    end;
    if not (cb <> 'b') then begin
        b8 := 8;
    end;

    writeln(b1, b2, b3, b4, b5, b6, b7, b8, '9', ' (char/bool test)');


    i := 1;
    while i <= 9 do begin
        s[i] := '0';
        i := i + 1;
    end;

    i := 1;
    while i <= 9 do begin
        if i < 3 then begin
            if i < 2 then begin
                s[i] := '1';
            end
            else begin
                s[i] := '2';
            end;
        end
        else begin
            s[i] := '3';
        end;

        ca := '4';
        if i >= 4 then begin
            if i <= 5 then begin
                if i <> 5 then begin
                    s[i] := ca;
                end
                else begin
                    s[i] := '5';
                end;
            end
            else begin
                ca := '9';
                if i < 7 then begin
                    s[i] := '6';
                end
                else begin
                    if i = 7 then begin
                        s[i] := '7';
                    end
                    else begin
                        if i <= 8 then begin
                            s[i] := '8';
                        end
                        else begin
                            s[i] := ca;
                        end;
                    end;
                end;
            end;
        end;
        i := i + 1;
    end;

    writeln(s[1], s[2], s[3], s[4], s[5], s[6], s[7], s[8], s[9], ' (string test)');
end.
