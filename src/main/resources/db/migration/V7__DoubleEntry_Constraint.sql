-- V7__DoubleEntry_Constraint.sql
-- Todo
create or replace function fn_check_double_entry() returns trigger as $$
declare
    _tid bigint;
    _cnt int;
    _sum numeric(19,4);
    _ccy_cnt int;
begin
    _tid := coalesce(new.transfer_id, old.transfer_id);

    select count(*) into _cnt
    from ledger_entries
    where transfer_id = _tid;
    if _cnt <> 2 then
        raise exception 'transfer % must have exactly 2 ledger entries, found %', _tid, _cnt;
    end if;

    select coalesce(sum(case when direction='CREDIT' then amount
                             when direction='DEBIT'  then -amount
                             else 0 end), 0)
    into _sum
    from ledger_entries
    where transfer_id = _tid;
    if _sum <> 0 then
        raise exception 'transfer % entries not balanced (sum=%)', _tid, _sum;
    end if;

    select count(distinct currency) into _ccy_cnt
    from ledger_entries
    where transfer_id = _tid;
    if _ccy_cnt <> 1 then
        raise exception 'transfer % has inconsistent currencies', _tid;
    end if;

    return null;
end;
$$ language plpgsql;

drop trigger if exists trg_ck_double_entry on ledger_entries;

create constraint trigger trg_ck_double_entry
after insert or update or delete on ledger_entries
deferrable initially deferred
for each row
execute function fn_check_double_entry();
