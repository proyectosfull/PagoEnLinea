SELECT cvcontrato, fcodcubre, fechacubre, freinstala, fcodreinstala, fcodbaja, fecbaja FROM controltomas WHERE fechacubre NOT LIKE '__/__/____' AND fechacubre NOT LIKE '____-__-__'

SELECT cvcontrato, fcodcubre, fechacubre, freinstala, fcodreinstala, fcodbaja, fecbaja FROM controltomas WHERE fechacubre NOT LIKE '__/__/____' AND freinstala = ''


SELECT count(*) FROM controltomas WHERE fechacubre LIKE '____-__-__'; * (yyyy-MM-dd)

SELECT count(*) FROM controltomas WHERE fechacubre LIKE '__/__/____'; * (dd/MM/yyyy)

SELECT cvcontrato, fechacubre, freinstala FROM controltomas WHERE cvcontrato NOT IN ((SELECT cvcontrato FROM controltomas WHERE fechacubre LIKE '____-__-__' AND (freinstala = '01/01/1900' OR freinstala = '1900-01-01' OR freinstala = '')) UNION (SELECT cvcontrato FROM controltomas WHERE fechacubre LIKE '____-__-__' AND (freinstala <> '01/01/1900' AND freinstala <> '1900-01-01' AND freinstala <> '')) UNION (SELECT cvcontrato FROM controltomas WHERE fechacubre LIKE '__/__/____' AND (freinstala = '01/01/1900' OR freinstala = '1900-01-01' OR freinstala = '')) UNION (SELECT cvcontrato FROM controltomas WHERE fechacubre LIKE '__/__/____' AND (freinstala <> '01/01/1900' AND freinstala <> '1900-01-01' AND freinstala <> '')) UNION (SELECT cvcontrato FROM controltomas WHERE fechacubre NOT LIKE '____-__-__' AND fechacubre NOT LIKE '__/__/____'));


// insert default value to second argument *
SELECT count(*) FROM controltomas WHERE fechacubre LIKE '____-__-__' AND (freinstala = '01/01/1900' OR freinstala = '1900-01-01' OR freinstala = '' OR freinstala IS null);

// apply format to second argument *
SELECT count(*) FROM controltomas WHERE fechacubre LIKE '____-__-__' AND (freinstala <> '01/01/1900' AND freinstala <> '1900-01-01' AND freinstala <> '' AND freinstala IS NOT NULL);

// apply format to first argument, insert default value to second argument *
SELECT count(*) FROM controltomas WHERE fechacubre LIKE '__/__/____' AND (freinstala = '01/01/1900' OR freinstala = '1900-01-01' OR freinstala = '' OR freinstala IS NULL);

//apply format to first and second argument *
SELECT count(*) FROM controltomas WHERE fechacubre LIKE '__/__/____' AND (freinstala <> '01/01/1900' AND freinstala <> '1900-01-01' AND freinstala <> '' AND freinstala IS NOT NULL);

// insert default value to both arguments
SELECT count(*) FROM controltomas WHERE fechacubre NOT LIKE '____-__-__' AND fechacubre NOT LIKE '__/__/____';

SELECT count(*) FROM pagoglobal WHERE fechareg NOT LIKE '____-__-__' AND fechareg NOT LIKE '__/__/____';

SELECT valor FROM rangosconsumo WHERE clave = (SELECT cvclasifica FROM giros WHERE cvgiros = 6) AND tipo = 'AGUA' AND '96' BETWEEN rangoInicial AND rangoFinal;

SELECT valor FROM rangosconsumo WHERE clave = (SELECT cvclasifica FROM giros WHERE cvgiros = 6) AND tipo = 'SANEAMIENTO' AND '72.00' BETWEEN rangoInicial AND rangoFinal;

////////////////////////////////////////////// pagoglobal

select count(*) from pagoglobal where fcubre like '________' and like fechaantes like '________';

select count(*) from pagoglobal where fcubre like '__/__/____' and like fechaantes like '________';

select count(*) from pagoglobal where fcubre like '________' and like fechaantes like '__/__/____';

select count(*) from pagoglobal where fcubre like '__/__/____' and like fechaantes like '__/__/____';