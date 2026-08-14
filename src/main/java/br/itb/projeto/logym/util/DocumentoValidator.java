package br.itb.projeto.logym.util;

public class DocumentoValidator {

    public static boolean isValidCPF(String cpf) {
        if (cpf == null) {
            return false;
        }

        String cleanCPF = cpf.replaceAll("\\D", "");

        if (cleanCPF.length() != 11) {
            return false;
        }

        if (cleanCPF.matches("(\\d)\\1{10}")) {
            return false;
        }

        int soma = 0;

        for (int i = 0; i < 9; i++) {
            soma += Character.getNumericValue(cleanCPF.charAt(i)) * (10 - i);
        }

        int primeiroDigito = 11 - (soma % 11);
        primeiroDigito = primeiroDigito >= 10 ? 0 : primeiroDigito;

        if (primeiroDigito != Character.getNumericValue(cleanCPF.charAt(9))) {
            return false;
        }

        soma = 0;

        for (int i = 0; i < 10; i++) {
            soma += Character.getNumericValue(cleanCPF.charAt(i)) * (11 - i);
        }

        int segundoDigito = 11 - (soma % 11);
        segundoDigito = segundoDigito >= 10 ? 0 : segundoDigito;

        return segundoDigito == Character.getNumericValue(cleanCPF.charAt(10));
    }

    public static boolean isValidCNPJ(String cnpj) {
        if (cnpj == null) {
            return false;
        }

        String cleanCNPJ = cnpj.replaceAll("\\D", "");

        if (cleanCNPJ.length() != 14) {
            return false;
        }

        if (cleanCNPJ.matches("(\\d)\\1{13}")) {
            return false;
        }

        int[] pesosPrimeiroDigito = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int[] pesosSegundoDigito = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};

        int primeiroDigito = calcularDigito(cleanCNPJ.substring(0, 12), pesosPrimeiroDigito);
        int segundoDigito = calcularDigito(cleanCNPJ.substring(0, 13), pesosSegundoDigito);

        return primeiroDigito == Character.getNumericValue(cleanCNPJ.charAt(12))
                && segundoDigito == Character.getNumericValue(cleanCNPJ.charAt(13));
    }

    private static int calcularDigito(String base, int[] pesos) {
        int soma = 0;

        for (int i = 0; i < pesos.length; i++) {
            soma += Character.getNumericValue(base.charAt(i)) * pesos[i];
        }

        int resto = soma % 11;

        return resto < 2 ? 0 : 11 - resto;
    }
}